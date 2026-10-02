package com.nowbox.nowbox_jobs.contrato;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContratoWorkerTest {

    @Mock
    private ContratoPdfGenerator pdfGenerator;

    @Mock
    private ContratoStorageService storageService;

    @Mock
    private ContratoRepository contratoRepository;

    @InjectMocks
    private ContratoWorker worker;

    @org.junit.jupiter.api.BeforeEach
    void aluguelAguardandoDocumento() {
        org.mockito.Mockito.lenient().when(contratoRepository.aguardandoGeracao(any())).thenReturn(true);
    }

    @Test
    @DisplayName("Should discard the request without generating anything when the signature was cancelled meanwhile")
    void processarAssinaturaCancelada() {
        ContratoSolicitadoMessage solicitacao = ContratoPdfGeneratorTest.solicitacao(true);
        when(contratoRepository.aguardandoGeracao(solicitacao)).thenReturn(false);

        worker.processar(solicitacao);

        verify(pdfGenerator, never()).gerar(any());
        verify(storageService, never()).gravar(any(), any(), any());
        verify(contratoRepository, never()).registrar(any(), any(), any(), any(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Should generate the PDF, store it in the bucket and register the arquivo in the database")
    void processarCase1() {
        ContratoSolicitadoMessage solicitacao = ContratoPdfGeneratorTest.solicitacao(true);
        byte[] pdf = {'%', 'P', 'D', 'F'};
        when(pdfGenerator.gerar(solicitacao)).thenReturn(pdf);
        when(storageService.getBucketContratos()).thenReturn("nowbox-contratos");
        when(contratoRepository.registrar(any(), anyString(), anyString(), anyString(), anyString(), anyLong(), any())).thenReturn(true);

        worker.processar(solicitacao);

        String chaveEsperada = "contratos/%s/%s/%s.pdf".formatted(solicitacao.idUnidade(), solicitacao.idAluguel(), solicitacao.idSolicitacao());
        verify(storageService).gravar(chaveEsperada, pdf, "application/pdf");
        verify(contratoRepository).registrar(eq(solicitacao), eq("nowbox-contratos"), eq(chaveEsperada),
                startsWith("contrato-teste-box-101-"), eq("application/pdf"), eq((long) pdf.length), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should store the aditivo under its own key and name so it never replaces the original contrato")
    void processarAditivo() {
        ContratoSolicitadoMessage solicitacao = ContratoPdfGeneratorTest.aditivo();
        byte[] pdf = {1, 2};
        when(pdfGenerator.gerar(solicitacao)).thenReturn(pdf);
        when(storageService.getBucketContratos()).thenReturn("nowbox-contratos");
        when(contratoRepository.registrar(any(), anyString(), anyString(), anyString(), anyString(), anyLong(), any())).thenReturn(true);

        worker.processar(solicitacao);

        String chaveEsperada = "contratos/%s/%s/aditivos/%s.pdf".formatted(solicitacao.idUnidade(), solicitacao.idAluguel(), solicitacao.idSolicitacao());
        verify(storageService).gravar(chaveEsperada, pdf, "application/pdf");
        verify(contratoRepository).registrar(eq(solicitacao), eq("nowbox-contratos"), eq(chaveEsperada),
                startsWith("aditivo-contrato-teste-box-101-"), eq("application/pdf"), eq((long) pdf.length), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should not register the arquivo when storing the PDF fails, so the message is retried")
    void processarCase2() {
        ContratoSolicitadoMessage solicitacao = ContratoPdfGeneratorTest.solicitacao(true);
        when(pdfGenerator.gerar(solicitacao)).thenReturn(new byte[]{1});
        doThrow(new IllegalStateException("minio fora do ar")).when(storageService).gravar(any(), any(), any());

        assertThatThrownBy(() -> worker.processar(solicitacao)).isInstanceOf(IllegalStateException.class);

        verify(contratoRepository, never()).registrar(any(), any(), any(), any(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Should let the error propagate when registering the arquivo fails, so the message is retried")
    void processarCase3() {
        ContratoSolicitadoMessage solicitacao = ContratoPdfGeneratorTest.solicitacao(true);
        when(pdfGenerator.gerar(solicitacao)).thenReturn(new byte[]{1});
        when(storageService.getBucketContratos()).thenReturn("nowbox-contratos");
        when(contratoRepository.registrar(any(), anyString(), anyString(), anyString(), anyString(), anyLong(), any()))
                .thenThrow(new IllegalStateException("banco fora do ar"));

        assertThatThrownBy(() -> worker.processar(solicitacao)).hasMessageContaining("banco fora do ar");
    }

    @Test
    @DisplayName("Should accept a redelivered message without failing when the arquivo was already registered")
    void processarCase4() {
        ContratoSolicitadoMessage solicitacao = ContratoPdfGeneratorTest.solicitacao(true);
        when(pdfGenerator.gerar(solicitacao)).thenReturn(new byte[]{1});
        when(storageService.getBucketContratos()).thenReturn("nowbox-contratos");
        when(contratoRepository.registrar(any(), anyString(), anyString(), anyString(), anyString(), anyLong(), any())).thenReturn(false);

        worker.processar(solicitacao);

        verify(storageService).gravar(anyString(), any(), anyString());
        assertThat(solicitacao).isNotNull();
    }

    @Test
    @DisplayName("Should store the distrato under its own key and name so it never replaces the original contrato")
    void processarDistrato() {
        ContratoSolicitadoMessage solicitacao = ContratoPdfGeneratorTest.distrato();
        byte[] pdf = {1, 2};
        when(pdfGenerator.gerar(solicitacao)).thenReturn(pdf);
        when(storageService.getBucketContratos()).thenReturn("nowbox-contratos");
        when(contratoRepository.registrar(any(), anyString(), anyString(), anyString(), anyString(), anyLong(), any())).thenReturn(true);

        worker.processar(solicitacao);

        String chaveEsperada = "contratos/%s/%s/distratos/%s.pdf".formatted(solicitacao.idUnidade(), solicitacao.idAluguel(), solicitacao.idSolicitacao());
        verify(storageService).gravar(chaveEsperada, pdf, "application/pdf");
        verify(contratoRepository).registrar(eq(solicitacao), eq("nowbox-contratos"), eq(chaveEsperada),
                startsWith("distrato-contrato-teste-box-101-"), eq("application/pdf"), eq((long) pdf.length), any(LocalDateTime.class));
    }
}
