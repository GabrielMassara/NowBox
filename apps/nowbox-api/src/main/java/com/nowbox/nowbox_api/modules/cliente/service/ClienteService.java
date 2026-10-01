package com.nowbox.nowbox_api.modules.cliente.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.common.exception.RequisicaoInvalidaException;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteCreateDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteFilterDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteResponseDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.DocumentoDownloadDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.DocumentoIdentidadeDTO;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
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
    public ClienteResponseDTO create(ClienteCreateDTO cliente, MultipartFile documento) throws NaoEncontradoException {

        // O documento é obrigatório no cadastro
        TipoDocumento tipo = validarDocumento(documento);

        Optional<EstadoEntity> estado = estadoRepository.findById(cliente.getIdEstado());

        // Se não encontrar o estado
        if(estado.isEmpty()) {
            throw new NaoEncontradoException("Estado inválido");
        }

        ArquivoEntity arquivo = salvarDocumento(documento, tipo);

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
                .enderecoCorrespondencia(cliente.getEnderecoCorrespondencia())
                .senha(cliente.getSenha())
                .senhaTemporaria(cliente.getSenhaTemporaria())
                .senhaTemporariaStatus(cliente.getSenhaTemporariaStatus())
                .build());

        return toResponseDTO(created);
    }

    @Transactional
    public ClienteResponseDTO update(ClienteCreateDTO cliente, MultipartFile documento, UUID id) throws NaoEncontradoException {
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
            arquivo = salvarDocumento(documento, validarDocumento(documento));
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
                .enderecoCorrespondencia(cliente.getEnderecoCorrespondencia())
                .senha(cliente.getSenha())
                .senhaTemporaria(cliente.getSenhaTemporaria())
                .senhaTemporariaStatus(cliente.getSenhaTemporariaStatus())
                .createdAt(existente.get().getCreatedAt())
                .build());

        // Documento substituído: o antigo deixa de ser referenciado e é apagado do MinIO depois do commit
        if(arquivo != documentoAnterior) {
            arquivoRepository.delete(documentoAnterior);
            executarAposCommit(() -> documentoStorageService.remover(documentoAnterior.getChave()));
        }

        return toResponseDTO(updated);
    }

    public DocumentoDownloadDTO downloadDocumento(UUID id) throws NaoEncontradoException {
        ClienteEntity cliente = clienteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));

        ArquivoEntity arquivo = cliente.getDocumentoIdentidade();
        if(arquivo == null) {
            throw new NaoEncontradoException("Cliente sem documento de identidade");
        }

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
                .enderecoCorrespondencia(entidade.getEnderecoCorrespondencia())
                .senhaTemporariaStatus(entidade.getSenhaTemporariaStatus())
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }

    private DocumentoIdentidadeDTO toDocumentoDTO(ArquivoEntity arquivo) {
        if(arquivo == null) {
            return null;
        }
        return new DocumentoIdentidadeDTO(arquivo.getId(), arquivo.getNomeOriginal(), arquivo.getContentType(), arquivo.getTamanho());
    }

    // Tipos aceitos para o documento. O tipo é identificado pelo conteúdo do arquivo, não pelo que o cliente informou
    private enum TipoDocumento {
        PDF("application/pdf", ".pdf"),
        JPEG("image/jpeg", ".jpg"),
        PNG("image/png", ".png"),
        WEBP("image/webp", ".webp");

        private final String contentType;
        private final String extensao;

        TipoDocumento(String contentType, String extensao) {
            this.contentType = contentType;
            this.extensao = extensao;
        }
    }

    private TipoDocumento validarDocumento(MultipartFile documento) {
        if(documento == null || documento.isEmpty()) {
            throw new RequisicaoInvalidaException("O documento de identidade é obrigatório");
        }

        byte[] inicio = new byte[12];
        int lidos;
        try (InputStream in = documento.getInputStream()) {
            lidos = in.readNBytes(inicio, 0, inicio.length);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler o documento enviado", e);
        }

        if(lidos >= 4 && inicio[0] == '%' && inicio[1] == 'P' && inicio[2] == 'D' && inicio[3] == 'F') {
            return TipoDocumento.PDF;
        }
        if(lidos >= 3 && (inicio[0] & 0xFF) == 0xFF && (inicio[1] & 0xFF) == 0xD8 && (inicio[2] & 0xFF) == 0xFF) {
            return TipoDocumento.JPEG;
        }
        if(lidos >= 4 && (inicio[0] & 0xFF) == 0x89 && inicio[1] == 'P' && inicio[2] == 'N' && inicio[3] == 'G') {
            return TipoDocumento.PNG;
        }
        if(lidos >= 12 && inicio[0] == 'R' && inicio[1] == 'I' && inicio[2] == 'F' && inicio[3] == 'F'
                && inicio[8] == 'W' && inicio[9] == 'E' && inicio[10] == 'B' && inicio[11] == 'P') {
            return TipoDocumento.WEBP;
        }

        throw new RequisicaoInvalidaException("O documento de identidade deve ser uma imagem (JPG, PNG ou WEBP) ou um PDF");
    }

    // Grava direto no MinIO (sem fila) e registra o arquivo. Se a transação for revertida, o objeto é apagado
    private ArquivoEntity salvarDocumento(MultipartFile documento, TipoDocumento tipo) {
        String chave = "clientes/documentos-identidade/" + UUID.randomUUID() + tipo.extensao;

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

    private void executarAposCommit(Runnable acao) {
        if(!TransactionSynchronizationManager.isSynchronizationActive()) {
            acao.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                acao.run();
            }
        });
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
