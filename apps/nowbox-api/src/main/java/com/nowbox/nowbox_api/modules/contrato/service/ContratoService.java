package com.nowbox.nowbox_api.modules.contrato.service;

import com.nowbox.nowbox_api.common.exception.ConflitoException;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.common.exception.RequisicaoInvalidaException;
import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.contrato.dto.AditivoResponseDTO;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoDownloadDTO;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoAluguelEntity;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import com.nowbox.nowbox_api.modules.contrato.repository.IArquivoAluguelRepository;
import com.nowbox.nowbox_api.modules.contrato.repository.IArquivoRepository;
import com.nowbox.nowbox_api.modules.contrato.storage.ContratoStorageService;
import com.nowbox.nowbox_api.modules.email.messaging.EmailSolicitadoMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final byte[] PDF_MAGIC = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final IArquivoAluguelRepository arquivoAluguelRepository;
    private final IAluguelRepository aluguelRepository;
    private final IArquivoRepository arquivoRepository;
    private final ContratoStorageService storageService;
    private final ApplicationEventPublisher eventPublisher;

    public Page<AditivoResponseDTO> listAditivosByAluguel(Pageable pageable, UUID idAluguel) throws NaoEncontradoException {
        aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        return arquivoAluguelRepository.findByAluguelIdOrderBySalvoEmDesc(idAluguel, pageable).map(this::toResponseDTO);
    }

    // Baixa um aditivo especifico
    public ContratoDownloadDTO downloadAditivo(UUID id) throws NaoEncontradoException {
        ArquivoAluguelEntity aditivo = arquivoAluguelRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Aditivo não encontrado"));

        return abrir(aditivo.getArquivo());
    }

    // Baixa o distrato assinado enviado depois da geracao do modelo
    public ContratoDownloadDTO downloadDistratoAssinado(UUID idAluguel) throws NaoEncontradoException {
        AluguelEntity aluguel = aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        if (aluguel.getDistratoAssinado() == null) {
            throw new NaoEncontradoException("O distrato assinado deste aluguel ainda não foi enviado");
        }

        return abrir(aluguel.getDistratoAssinado());
    }

    // Baixa o aditivo assinado enviado depois da geracao
    public ContratoDownloadDTO downloadAditivoAssinado(UUID id) throws NaoEncontradoException {
        ArquivoAluguelEntity aditivo = arquivoAluguelRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Aditivo não encontrado"));

        if (aditivo.getAssinado() == null) {
            throw new NaoEncontradoException("O aditivo assinado ainda não foi enviado");
        }

        return abrir(aditivo.getAssinado());
    }

    // Baixa o contrato original do aluguel. Ele nao muda quando o aluguel e editado
    public ContratoDownloadDTO downloadContrato(UUID idAluguel) throws NaoEncontradoException {
        AluguelEntity aluguel = aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        if (aluguel.getContrato() == null) {
            throw new NaoEncontradoException("O contrato deste aluguel ainda não foi gerado");
        }

        return abrir(aluguel.getContrato());
    }

    // Baixa o contrato assinado enviado depois da geracao do modelo
    public ContratoDownloadDTO downloadContratoAssinado(UUID idAluguel) throws NaoEncontradoException {
        AluguelEntity aluguel = aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        if (aluguel.getContratoAssinado() == null) {
            throw new NaoEncontradoException("O contrato assinado deste aluguel ainda não foi enviado");
        }

        return abrir(aluguel.getContratoAssinado());
    }

    // Baixa o distrato gerado quando o encerramento do aluguel foi iniciado
    public ContratoDownloadDTO downloadDistrato(UUID idAluguel) throws NaoEncontradoException {
        AluguelEntity aluguel = aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        if (aluguel.getDistrato() == null) {
            throw new NaoEncontradoException("O distrato deste aluguel ainda não foi gerado");
        }

        return abrir(aluguel.getDistrato());
    }

    // Recebe o contrato assinado pelo cliente. Com ele guardado o aluguel passa a valer e fica ativo
    @Transactional
    public void enviarContratoAssinado(UUID idAluguel, MultipartFile arquivo) throws NaoEncontradoException {
        AluguelEntity aluguel = buscarComStatus(idAluguel, StatusAluguel.PENDENTE_ASSINATURA_CONTRATO,
                "O aluguel não está aguardando a assinatura do contrato");

        if (aluguel.getContrato() == null) {
            throw new ConflitoException("O contrato deste aluguel ainda não foi gerado");
        }

        aluguel.setContratoAssinado(guardarAssinado(aluguel, arquivo, "contratos-assinados", "contrato-assinado"));
        aluguel.setStatus(StatusAluguel.ATIVO);
        aluguelRepository.save(aluguel);

        solicitarEmail(aluguel, EmailSolicitadoMessage::aluguelRegistrado);
    }

    // Recebe o aditivo assinado. Com ele guardado as alteracoes passam a valer e o aluguel volta a ficar ativo
    @Transactional
    public void enviarAditivoAssinado(UUID idAluguel, MultipartFile arquivo) throws NaoEncontradoException {
        AluguelEntity aluguel = buscarComStatus(idAluguel, StatusAluguel.PENDENTE_ASSINATURA_ADITIVO,
                "O aluguel não está aguardando a assinatura de um aditivo");

        ArquivoAluguelEntity aditivo = arquivoAluguelRepository.findFirstByAluguelIdAndPendenteAssinaturaTrueOrderBySalvoEmDesc(idAluguel)
                .orElseThrow(() -> new ConflitoException("O aditivo deste aluguel ainda não foi gerado"));

        aditivo.setAssinado(guardarAssinado(aluguel, arquivo, "aditivos-assinados", "aditivo-assinado"));
        aditivo.setPendenteAssinatura(false);
        arquivoAluguelRepository.save(aditivo);

        // As alteracoes foram aceitas, entao nao ha mais o que restaurar
        aluguel.setBoxAnterior(null);
        aluguel.setClienteAnterior(null);
        aluguel.setValorAnterior(null);
        aluguel.setObservacaoAnterior(null);
        aluguel.setStatus(StatusAluguel.ATIVO);
        aluguelRepository.save(aluguel);
    }

    // Recebe o distrato assinado pelas duas partes. Com ele guardado o contrato e de fato encerrado e o aluguel fica inativo
    @Transactional
    public void enviarDistratoAssinado(UUID idAluguel, MultipartFile arquivo) throws NaoEncontradoException {
        AluguelEntity aluguel = buscarComStatus(idAluguel, StatusAluguel.PENDENTE_ASSINATURA_DISTRATO,
                "O aluguel não está aguardando a assinatura do distrato");

        if (aluguel.getDistrato() == null) {
            throw new ConflitoException("O distrato deste aluguel ainda não foi gerado");
        }

        aluguel.setDistratoAssinado(guardarAssinado(aluguel, arquivo, "distratos-assinados", "distrato-assinado"));
        aluguel.setStatus(StatusAluguel.INATIVO);
        aluguelRepository.save(aluguel);

        solicitarEmail(aluguel, EmailSolicitadoMessage::aluguelEncerrado);
    }

    private AluguelEntity buscarComStatus(UUID idAluguel, StatusAluguel esperado, String mensagemConflito) {
        AluguelEntity aluguel = aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        if (aluguel.getStatus() != esperado) {
            throw new ConflitoException(mensagemConflito);
        }

        return aluguel;
    }

    // Grava o PDF assinado no MinIO e registra a referencia dele. Se o registro falhar o objeto e removido
    private ArquivoEntity guardarAssinado(AluguelEntity aluguel, MultipartFile arquivo, String pasta, String prefixoNome) {
        validarPdf(arquivo);

        String chave = "contratos/%s/%s/%s/%s.pdf".formatted(aluguel.getBox().getUnidade().getId(), aluguel.getId(), pasta, UUID.randomUUID());

        try (InputStream conteudo = arquivo.getInputStream()) {
            storageService.gravar(chave, conteudo, arquivo.getSize(), CONTENT_TYPE_PDF);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler o arquivo enviado", e);
        }

        try {
            return arquivoRepository.save(ArquivoEntity.builder()
                    .bucket(storageService.getBucketContratos())
                    .chave(chave)
                    .nomeOriginal(nomeDoArquivo(arquivo, prefixoNome + "-box-" + aluguel.getBox().getNumero() + ".pdf"))
                    .contentType(CONTENT_TYPE_PDF)
                    .tamanho(arquivo.getSize())
                    .build());
        } catch (RuntimeException e) {
            storageService.remover(chave);
            throw e;
        }
    }

    // Aceita somente PDF, conferindo a assinatura do arquivo e nao apenas o tipo informado pelo navegador
    private void validarPdf(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RequisicaoInvalidaException("Envie o arquivo assinado em PDF");
        }

        try (InputStream conteudo = arquivo.getInputStream()) {
            if (!Arrays.equals(conteudo.readNBytes(PDF_MAGIC.length), PDF_MAGIC)) {
                throw new RequisicaoInvalidaException("O arquivo assinado precisa ser um PDF");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler o arquivo enviado", e);
        }
    }

    private String nomeDoArquivo(MultipartFile arquivo, String padrao) {
        String nome = StringUtils.getFilename(arquivo.getOriginalFilename());
        return StringUtils.hasText(nome) && nome.length() <= 200 ? nome : padrao;
    }

    // Avisa o cliente, se ele tiver email cadastrado. O envio e assincrono e so acontece depois do commit
    private void solicitarEmail(AluguelEntity aluguel, Function<AluguelEntity, EmailSolicitadoMessage> mensagem) {
        if (StringUtils.hasText(aluguel.getCliente().getEmail())) {
            eventPublisher.publishEvent(mensagem.apply(aluguel));
        }
    }

    private ContratoDownloadDTO abrir(ArquivoEntity arquivo) {
        return new ContratoDownloadDTO(
                arquivo.getNomeOriginal(),
                arquivo.getContentType(),
                arquivo.getTamanho(),
                storageService.abrir(arquivo.getChave())
        );
    }

    private AditivoResponseDTO toResponseDTO(ArquivoAluguelEntity entidade) {
        return AditivoResponseDTO.builder()
                .id(entidade.getId())
                .idAluguel(entidade.getAluguel().getId())
                .idBox(entidade.getBox().getId())
                .numeroBox(entidade.getBox().getNumero())
                .nomeCliente(entidade.getAluguel().getCliente().getNome())
                .nomeArquivo(entidade.getArquivo().getNomeOriginal())
                .tamanho(entidade.getArquivo().getTamanho())
                .descricao(entidade.getDescricao())
                .salvoEm(entidade.getSalvoEm())
                .pendenteAssinatura(entidade.isPendenteAssinatura())
                .assinado(entidade.getAssinado() != null)
                .cancelado(entidade.isCancelado())
                .build();
    }
}
