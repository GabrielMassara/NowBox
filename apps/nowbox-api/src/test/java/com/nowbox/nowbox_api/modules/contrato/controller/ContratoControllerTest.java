package com.nowbox.nowbox_api.modules.contrato.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoDownloadDTO;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoResponseDTO;
import com.nowbox.nowbox_api.modules.contrato.service.ContratoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContratoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContratoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContratoService contratoService;

    private ContratoResponseDTO dto() {
        return ContratoResponseDTO.builder().id(UUID.randomUUID()).numeroBox("101").nomeCliente("Cliente").nomeArquivo("contrato-teste.pdf").build();
    }

    @Test
    @DisplayName("Should list the contratos of an aluguel with status 200")
    void listByAluguel() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        when(contratoService.listByAluguel(any(), eq(idAluguel))).thenReturn(new PageImpl<>(List.of(dto())));

        mockMvc.perform(get("/v1/contrato/aluguel/{id}", idAluguel))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numeroBox").value("101"))
                .andExpect(jsonPath("$.content[0].nomeArquivo").value("contrato-teste.pdf"));

        verify(contratoService).listByAluguel(any(), eq(idAluguel));
    }

    @Test
    @DisplayName("Should list the contratos of a box with status 200")
    void listByBox() throws Exception {
        UUID idBox = UUID.randomUUID();
        when(contratoService.listByBox(any(), eq(idBox))).thenReturn(new PageImpl<>(List.of(dto())));

        mockMvc.perform(get("/v1/contrato/box/{id}", idBox))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nomeCliente").value("Cliente"));

        verify(contratoService).listByBox(any(), eq(idBox));
    }

    @Test
    @DisplayName("Should return status 404 when listing contratos of an aluguel that does not exist")
    void listByAluguelNotFound() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        when(contratoService.listByAluguel(any(), eq(idAluguel))).thenThrow(new NaoEncontradoException("Aluguel não encontrado"));

        mockMvc.perform(get("/v1/contrato/aluguel/{id}", idAluguel))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should download a specific contrato as an attachment PDF")
    void download() throws Exception {
        UUID id = UUID.randomUUID();
        when(contratoService.download(id)).thenReturn(new ContratoDownloadDTO("contrato-teste.pdf", "application/pdf", 3L, new ByteArrayInputStream(new byte[]{1, 2, 3})));

        mockMvc.perform(get("/v1/contrato/{id}/download", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("contrato-teste.pdf")))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    @DisplayName("Should download the current contrato of an aluguel")
    void downloadAtual() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        when(contratoService.downloadAtual(idAluguel)).thenReturn(new ContratoDownloadDTO("atual.pdf", "application/pdf", 1L, new ByteArrayInputStream(new byte[]{9})));

        mockMvc.perform(get("/v1/contrato/aluguel/{id}/atual/download", idAluguel))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("atual.pdf")));
    }

    @Test
    @DisplayName("Should return status 404 when the contrato was not generated yet")
    void downloadAtualNotFound() throws Exception {
        UUID idAluguel = UUID.randomUUID();
        when(contratoService.downloadAtual(idAluguel)).thenThrow(new NaoEncontradoException("O contrato deste aluguel ainda não foi gerado"));

        mockMvc.perform(get("/v1/contrato/aluguel/{id}/atual/download", idAluguel))
                .andExpect(status().isNotFound());
    }
}
