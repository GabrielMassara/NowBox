package com.nowbox.nowbox_api.modules.contrato.controller;

import com.nowbox.nowbox_api.common.exception.ConflitoException;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoDownloadDTO;
import com.nowbox.nowbox_api.modules.contrato.dto.AditivoResponseDTO;
import com.nowbox.nowbox_api.modules.contrato.service.ContratoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContratoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContratoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContratoService contratoService;

    private AditivoResponseDTO dto() {
        return AditivoResponseDTO.builder().id(UUID.randomUUID()).numeroBox("101").nomeCliente("Cliente").nomeArquivo("contrato-teste.pdf").build();
    }

    @Test
    @DisplayName("Should list the contratos of an aluguel with status 200")
    void listAditivosByAluguel() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        when(contratoService.listAditivosByAluguel(any(), eq(idAluguel))).thenReturn(new PageImpl<>(List.of(dto())));

        mockMvc.perform(get("/v1/contrato/aluguel/{id}/aditivos", idAluguel))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numeroBox").value("101"))
                .andExpect(jsonPath("$.content[0].nomeArquivo").value("contrato-teste.pdf"));

        verify(contratoService).listAditivosByAluguel(any(), eq(idAluguel));
    }

    @Test
    @DisplayName("Should return status 404 when listing contratos of an aluguel that does not exist")
    void listAditivosByAluguelNotFound() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        when(contratoService.listAditivosByAluguel(any(), eq(idAluguel))).thenThrow(new NaoEncontradoException("Aluguel não encontrado"));

        mockMvc.perform(get("/v1/contrato/aluguel/{id}/aditivos", idAluguel))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should download a specific aditivo as an attachment PDF")
    void download() throws Exception {
        UUID id = UUID.randomUUID();
        when(contratoService.downloadAditivo(id)).thenReturn(new ContratoDownloadDTO("contrato-teste.pdf", "application/pdf", 3L, new ByteArrayInputStream(new byte[]{1, 2, 3})));

        mockMvc.perform(get("/v1/contrato/aditivo/{id}/download", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("contrato-teste.pdf")))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    @DisplayName("Should download the original contrato of an aluguel")
    void downloadContrato() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        when(contratoService.downloadContrato(idAluguel)).thenReturn(new ContratoDownloadDTO("atual.pdf", "application/pdf", 1L, new ByteArrayInputStream(new byte[]{9})));

        mockMvc.perform(get("/v1/contrato/aluguel/{id}/download", idAluguel))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("atual.pdf")));
    }

    @Test
    @DisplayName("Should return status 404 when the contrato was not generated yet")
    void downloadContratoNotFound() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        when(contratoService.downloadContrato(idAluguel)).thenThrow(new NaoEncontradoException("O contrato deste aluguel ainda não foi gerado"));

        mockMvc.perform(get("/v1/contrato/aluguel/{id}/download", idAluguel))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should download the distrato of an aluguel")
    void downloadDistrato() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        when(contratoService.downloadDistrato(idAluguel)).thenReturn(new ContratoDownloadDTO("distrato.pdf", "application/pdf", 1L, new ByteArrayInputStream(new byte[]{9})));

        mockMvc.perform(get("/v1/contrato/aluguel/{id}/distrato/download", idAluguel))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("distrato.pdf")));
    }

    @Test
    @DisplayName("Should receive the signed contract with status 204")
    void enviarContratoAssinado() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "assinado.pdf", "application/pdf", "%PDF-1.7".getBytes());

        mockMvc.perform(multipart("/v1/contrato/aluguel/{id}/contrato-assinado", idAluguel).file(arquivo))
                .andExpect(status().isNoContent());

        verify(contratoService).enviarContratoAssinado(eq(idAluguel), any());
    }

    @Test
    @DisplayName("Should return status 409 when the aluguel is not pending the contract signature")
    void enviarContratoAssinadoConflito() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "assinado.pdf", "application/pdf", "%PDF-1.7".getBytes());
        doThrow(new ConflitoException("O aluguel não está aguardando a assinatura do contrato")).when(contratoService).enviarContratoAssinado(eq(idAluguel), any());

        mockMvc.perform(multipart("/v1/contrato/aluguel/{id}/contrato-assinado", idAluguel).file(arquivo))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should receive the signed distrato with status 204")
    void enviarDistratoAssinado() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "distrato.pdf", "application/pdf", "%PDF-1.7".getBytes());

        mockMvc.perform(multipart("/v1/contrato/aluguel/{id}/distrato-assinado", idAluguel).file(arquivo))
                .andExpect(status().isNoContent());

        verify(contratoService).enviarDistratoAssinado(eq(idAluguel), any());
    }
}
