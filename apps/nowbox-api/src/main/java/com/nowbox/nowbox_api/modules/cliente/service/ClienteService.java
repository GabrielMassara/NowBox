package com.nowbox.nowbox_api.modules.cliente.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.common.exception.RequisicaoInvalidaException;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteCreateDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteFilterDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteResponseDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.DocumentoDownloadDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.DocumentoHistoricoDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.DocumentoClienteDTO;
import com.nowbox.nowbox_api.modules.cliente.entity.ArquivoClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.TipoDocumentoCliente;
import com.nowbox.nowbox_api.modules.cliente.repository.IArquivoClienteRepository;
import com.nowbox.nowbox_api.modules.cliente.repository.IClienteRepository;
import com.nowbox.nowbox_api.modules.cliente.storage.DocumentoStorageService;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import com.nowbox.nowbox_api.modules.contrato.repository.IArquivoRepository;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.estado.repository.IEstadoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final IClienteRepository clienteRepository;
    private final IEstadoRepository estadoRepository;
    private final IArquivoRepository arquivoRepository;
    private final IArquivoClienteRepository arquivoClienteRepository;
    private final DocumentoStorageService documentoStorageService;

    public Page<ClienteResponseDTO> listAllByFilter(Pageable pageable, ClienteFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        UUID idEstado = null;
        String nome = null;
        String cpf = null;
        String email = null;

        if(filtro != null) {
            if(StringUtils.hasText(String.valueOf(filtro.getIdEstado()))) {
                idEstado = filtro.getIdEstado();
            }
            if(StringUtils.hasText(filtro.getNome())) {
                nome = filtro.getNome();
            }
            if(StringUtils.hasText(filtro.getCpf())) {
                cpf = filtro.getCpf();
            }
            if(StringUtils.hasText(filtro.getEmail())) {
                email = filtro.getEmail();
            }
        }

        return clienteRepository.findAllByFilter(idEstado, nome, cpf, email, pageable).map(this::toResponseDTO);
    }

    public ClienteResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<ClienteEntity> encontrado = clienteRepository.findByIdAndDeletedAtIsNull(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Cliente não encontrado");
        }

        return toResponseDTO(encontrado.get());
    }

    @Transactional
    public ClienteResponseDTO create(ClienteCreateDTO cliente, MultipartFile documento, MultipartFile comprovante) throws NaoEncontradoException {

        // Identidade e comprovante de residência são obrigatórios no cadastro
        TipoArquivo tipoDocumento = validarDocumento(documento, TipoDocumentoCliente.IDENTIDADE);
        TipoArquivo tipoComprovante = validarDocumento(comprovante, TipoDocumentoCliente.COMPROVANTE_RESIDENCIA);

        Optional<EstadoEntity> estado = estadoRepository.findById(cliente.getIdEstado());

        // Se não encontrar o estado
        if(estado.isEmpty()) {
            throw new NaoEncontradoException("Estado inválido");
        }

        ArquivoEntity arquivo = salvarDocumento(documento, tipoDocumento, TipoDocumentoCliente.IDENTIDADE);
        ArquivoEntity arquivoComprovante = salvarDocumento(comprovante, tipoComprovante, TipoDocumentoCliente.COMPROVANTE_RESIDENCIA);

        ClienteEntity created = clienteRepository.save(ClienteEntity.builder()
                .nome(cliente.getNome())
                .profissao(cliente.getProfissao())
                .cpf(cliente.getCpf())
                .rg(cliente.getRg())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .sexo(cliente.getSexo())
                .nascimento(cliente.getNascimento())
                .endereco(cliente.getEndereco())
                .numero(cliente.getNumero())
                .complemento(cliente.getComplemento())
                .bairro(cliente.getBairro())
                .cep(cliente.getCep())
                .cidade(cliente.getCidade())
                .estado(estado.get())
                .documentoIdentidade(arquivo)
                .comprovanteResidencia(arquivoComprovante)
                .enderecoCorrespondencia(cliente.getEnderecoCorrespondencia())
                .senha(cliente.getSenha())
                .senhaTemporaria(cliente.getSenhaTemporaria())
                .senhaTemporariaStatus(cliente.getSenhaTemporariaStatus())
                .build());

        registrarHistorico(arquivo, created, TipoDocumentoCliente.IDENTIDADE);
        registrarHistorico(arquivoComprovante, created, TipoDocumentoCliente.COMPROVANTE_RESIDENCIA);

        return toResponseDTO(created);
    }

    @Transactional
    public ClienteResponseDTO update(ClienteCreateDTO cliente, MultipartFile documento, MultipartFile comprovante, UUID id) throws NaoEncontradoException {
        // verifica se existe o registro
        Optional<ClienteEntity> existente = clienteRepository.findByIdAndDeletedAtIsNull(id);
        if(existente.isEmpty()) {
            throw new NaoEncontradoException("Cliente não encontrado");
        }

        // busca o estado
        Optional<EstadoEntity> estado = estadoRepository.findById(cliente.getIdEstado());
        if(estado.isEmpty()) {
            throw new NaoEncontradoException("Estado não encontrado");
        }

        // Sem novo documento mantém o atual
        ArquivoEntity documentoAnterior = existente.get().getDocumentoIdentidade();
        ArquivoEntity arquivo = documentoAnterior;
        if(documento != null && !documento.isEmpty()) {
            arquivo = salvarDocumento(documento, validarDocumento(documento, TipoDocumentoCliente.IDENTIDADE), TipoDocumentoCliente.IDENTIDADE);
        }

        ArquivoEntity comprovanteAnterior = existente.get().getComprovanteResidencia();
        ArquivoEntity arquivoComprovante = comprovanteAnterior;
        if(comprovante != null && !comprovante.isEmpty()) {
            arquivoComprovante = salvarDocumento(comprovante, validarDocumento(comprovante, TipoDocumentoCliente.COMPROVANTE_RESIDENCIA), TipoDocumentoCliente.COMPROVANTE_RESIDENCIA);
        }

        ClienteEntity updated = clienteRepository.save(ClienteEntity.builder()
                .id(id)
                .nome(cliente.getNome())
                .profissao(cliente.getProfissao())
                .cpf(cliente.getCpf())
                .rg(cliente.getRg())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .sexo(cliente.getSexo())
                .nascimento(cliente.getNascimento())
                .endereco(cliente.getEndereco())
                .numero(cliente.getNumero())
                .complemento(cliente.getComplemento())
                .bairro(cliente.getBairro())
                .cep(cliente.getCep())
                .cidade(cliente.getCidade())
                .estado(estado.get())
                .documentoIdentidade(arquivo)
                .comprovanteResidencia(arquivoComprovante)
                .enderecoCorrespondencia(cliente.getEnderecoCorrespondencia())
                .senha(cliente.getSenha())
                .senhaTemporaria(cliente.getSenhaTemporaria())
                .senhaTemporariaStatus(cliente.getSenhaTemporariaStatus())
                .createdAt(existente.get().getCreatedAt())
                .build());

        // Documento substituído: o novo entra no histórico e o antigo permanece guardado no MinIO
        if(arquivo != documentoAnterior) {
            registrarHistorico(arquivo, updated, TipoDocumentoCliente.IDENTIDADE);
        }
        if(arquivoComprovante != comprovanteAnterior) {
            registrarHistorico(arquivoComprovante, updated, TipoDocumentoCliente.COMPROVANTE_RESIDENCIA);
        }

        return toResponseDTO(updated);
    }

    public DocumentoDownloadDTO downloadDocumento(UUID id, TipoDocumentoCliente tipo) throws NaoEncontradoException {
        ClienteEntity cliente = clienteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));

        ArquivoEntity arquivo = documentoAtual(cliente, tipo);
        if(arquivo == null) {
            throw new NaoEncontradoException("Cliente sem " + tipo.getDescricao());
        }

        return new DocumentoDownloadDTO(
                arquivo.getNomeOriginal(),
                arquivo.getContentType(),
                arquivo.getTamanho(),
                documentoStorageService.abrir(arquivo.getChave()));
    }

    public Page<DocumentoHistoricoDTO> listDocumentos(Pageable pageable, UUID id, TipoDocumentoCliente tipo) throws NaoEncontradoException {
        ClienteEntity cliente = clienteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));

        ArquivoEntity atual = documentoAtual(cliente, tipo);
        UUID idAtual = atual != null ? atual.getId() : null;

        return arquivoClienteRepository.findByClienteIdAndTipoOrderBySalvoEmDesc(id, tipo, pageable).map(historico -> toHistoricoDTO(historico, idAtual));
    }

    // Baixa uma versao especifica do historico
    public DocumentoDownloadDTO downloadHistorico(UUID id, UUID idDocumento) throws NaoEncontradoException {
        clienteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));

        ArquivoEntity arquivo = arquivoClienteRepository.findByIdAndClienteId(idDocumento, id)
                .orElseThrow(() -> new NaoEncontradoException("Documento não encontrado"))
                .getArquivo();

        return new DocumentoDownloadDTO(
                arquivo.getNomeOriginal(),
                arquivo.getContentType(),
                arquivo.getTamanho(),
                documentoStorageService.abrir(arquivo.getChave()));
    }

    @Transactional
    public void delete(UUID id) throws NaoEncontradoException {
        ClienteEntity existente = clienteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));

        existente.setDeletedAt(LocalDateTime.now());
        clienteRepository.save(existente);
    }

    private ClienteResponseDTO toResponseDTO(ClienteEntity entidade) {
        return ClienteResponseDTO.builder()
                .id(entidade.getId())
                .estado(entidade.getEstado())
                .nome(entidade.getNome())
                .profissao(entidade.getProfissao())
                .cpf(entidade.getCpf())
                .rg(entidade.getRg())
                .email(entidade.getEmail())
                .telefone(entidade.getTelefone())
                .sexo(entidade.getSexo())
                .nascimento(entidade.getNascimento())
                .endereco(entidade.getEndereco())
                .numero(entidade.getNumero())
                .complemento(entidade.getComplemento())
                .bairro(entidade.getBairro())
                .cep(entidade.getCep())
                .cidade(entidade.getCidade())
                .documentoIdentidade(toDocumentoDTO(entidade.getDocumentoIdentidade()))
                .comprovanteResidencia(toDocumentoDTO(entidade.getComprovanteResidencia()))
                .enderecoCorrespondencia(entidade.getEnderecoCorrespondencia())
                .senhaTemporariaStatus(entidade.getSenhaTemporariaStatus())
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }

    private DocumentoHistoricoDTO toHistoricoDTO(ArquivoClienteEntity historico, UUID idAtual) {
        ArquivoEntity arquivo = historico.getArquivo();
        return new DocumentoHistoricoDTO(historico.getId(), arquivo.getNomeOriginal(), arquivo.getContentType(), arquivo.getTamanho(),
                historico.getSalvoEm(), arquivo.getId().equals(idAtual));
    }

    private ArquivoEntity documentoAtual(ClienteEntity cliente, TipoDocumentoCliente tipo) {
        return tipo == TipoDocumentoCliente.IDENTIDADE ? cliente.getDocumentoIdentidade() : cliente.getComprovanteResidencia();
    }

    private void registrarHistorico(ArquivoEntity arquivo, ClienteEntity cliente, TipoDocumentoCliente tipo) {
        arquivoClienteRepository.save(ArquivoClienteEntity.builder()
                .arquivo(arquivo)
                .cliente(cliente)
                .tipo(tipo)
                .salvoEm(LocalDateTime.now())
                .build());
    }

    private DocumentoClienteDTO toDocumentoDTO(ArquivoEntity arquivo) {
        if(arquivo == null) {
            return null;
        }
        return new DocumentoClienteDTO(arquivo.getId(), arquivo.getNomeOriginal(), arquivo.getContentType(), arquivo.getTamanho());
    }

    // Tipos aceitos para o documento. O tipo é identificado pelo conteúdo do arquivo, não pelo que o cliente informou
    private enum TipoArquivo {
        PDF("application/pdf", ".pdf"),
        JPEG("image/jpeg", ".jpg"),
        PNG("image/png", ".png"),
        WEBP("image/webp", ".webp");

        private final String contentType;
        private final String extensao;

        TipoArquivo(String contentType, String extensao) {
            this.contentType = contentType;
            this.extensao = extensao;
        }
    }

    private TipoArquivo validarDocumento(MultipartFile documento, TipoDocumentoCliente tipoDocumento) {
        if(documento == null || documento.isEmpty()) {
            throw new RequisicaoInvalidaException("O " + tipoDocumento.getDescricao() + " é obrigatório");
        }

        byte[] inicio = new byte[12];
        int lidos;
        try (InputStream in = documento.getInputStream()) {
            lidos = in.readNBytes(inicio, 0, inicio.length);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler o documento enviado", e);
        }

        if(lidos >= 4 && inicio[0] == '%' && inicio[1] == 'P' && inicio[2] == 'D' && inicio[3] == 'F') {
            return TipoArquivo.PDF;
        }
        if(lidos >= 3 && (inicio[0] & 0xFF) == 0xFF && (inicio[1] & 0xFF) == 0xD8 && (inicio[2] & 0xFF) == 0xFF) {
            return TipoArquivo.JPEG;
        }
        if(lidos >= 4 && (inicio[0] & 0xFF) == 0x89 && inicio[1] == 'P' && inicio[2] == 'N' && inicio[3] == 'G') {
            return TipoArquivo.PNG;
        }
        if(lidos >= 12 && inicio[0] == 'R' && inicio[1] == 'I' && inicio[2] == 'F' && inicio[3] == 'F'
                && inicio[8] == 'W' && inicio[9] == 'E' && inicio[10] == 'B' && inicio[11] == 'P') {
            return TipoArquivo.WEBP;
        }

        throw new RequisicaoInvalidaException("O " + tipoDocumento.getDescricao() + " deve ser uma imagem (JPG, PNG ou WEBP) ou um PDF");
    }

    // Grava direto no MinIO (sem fila) e registra o arquivo. Se a transação for revertida, o objeto é apagado
    private ArquivoEntity salvarDocumento(MultipartFile documento, TipoArquivo tipo, TipoDocumentoCliente tipoDocumento) {
        String chave = tipoDocumento.getPrefixoChave() + UUID.randomUUID() + tipo.extensao;

        try (InputStream in = documento.getInputStream()) {
            documentoStorageService.salvar(chave, in, documento.getSize(), tipo.contentType);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler o documento enviado", e);
        }

        executarAposRollback(() -> documentoStorageService.remover(chave));

        String nome = StringUtils.getFilename(documento.getOriginalFilename());
        if(!StringUtils.hasText(nome)) {
            nome = "documento" + tipo.extensao;
        }
        if(nome.length() > 200) {
            nome = nome.substring(nome.length() - 200);
        }

        return arquivoRepository.save(ArquivoEntity.builder()
                .bucket(documentoStorageService.getBucket())
                .chave(chave)
                .nomeOriginal(nome)
                .contentType(tipo.contentType)
                .tamanho(documento.getSize())
                .build());
    }

    private void executarAposRollback(Runnable acao) {
        if(!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if(status != STATUS_COMMITTED) {
                    acao.run();
                }
            }
        });
    }
}
