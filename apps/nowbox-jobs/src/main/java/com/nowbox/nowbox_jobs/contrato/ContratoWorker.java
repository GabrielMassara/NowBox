package com.nowbox.nowbox_jobs.contrato;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Consome as solicitacoes de contrato, gera o PDF e grava no MinIO e registra a referencia do arquivo no banco
@Slf4j
@Component
@RequiredArgsConstructor
public class ContratoWorker {

    static final String CONTENT_TYPE_PDF = "application/pdf";

    private static final DateTimeFormatter CARIMBO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ContratoPdfGenerator pdfGenerator;
    private final ContratoStorageService storageService;
    private final ContratoRepository contratoRepository;

    @RabbitListener(queues = ContratoMessagingConfig.FILA_SOLICITADO)
    public void processar(ContratoSolicitadoMessage solicitacao) {
        log.info("Gerando contrato do aluguel {} (solicitacao {})", solicitacao.idAluguel(), solicitacao.idSolicitacao());

        byte[] pdf = pdfGenerator.gerar(solicitacao);
        LocalDateTime geradoEm = LocalDateTime.now();

        String chave = "contratos/%s/%s/%s.pdf".formatted(solicitacao.idUnidade(), solicitacao.idAluguel(), solicitacao.idSolicitacao());
        String nomeArquivo = "contrato-teste-box-%s-%s.pdf".formatted(solicitacao.numeroBox(), CARIMBO.format(geradoEm));

        storageService.gravar(chave, pdf, CONTENT_TYPE_PDF);

        // Se o registro falhar, a mensagem e reprocessada e o objeto do MinIO é regravado com a mesma chave
        boolean registrado = contratoRepository.registrar(solicitacao, storageService.getBucketContratos(), chave, nomeArquivo,
                CONTENT_TYPE_PDF, pdf.length, geradoEm);

        log.info(registrado ? "Contrato do aluguel {} gravado em {}" : "Contrato do aluguel {} ja estava registrado em {}", solicitacao.idAluguel(), chave);
    }
}
