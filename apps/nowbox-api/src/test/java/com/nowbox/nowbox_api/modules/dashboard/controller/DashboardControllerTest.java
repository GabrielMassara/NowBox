package com.nowbox.nowbox_api.modules.dashboard.controller;

import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardAluguelStatusDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardEvolucaoDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardOcupacaoDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardPendenciaDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardResumoDTO;
import com.nowbox.nowbox_api.modules.dashboard.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should return the resumo with status 200")
    void resumo() throws Exception {
        UUID idUnidade = UUID.randomUUID();

        DashboardResumoDTO dto = DashboardResumoDTO.builder()
                .totalBoxes(8).taxaOcupacao(BigDecimal.valueOf(37.5)).alugueisVigentes(3).clientesAtivos(2)
                .receitaMensal(BigDecimal.valueOf(450.00)).build();
        when(dashboardService.resumo(idUnidade)).thenReturn(dto);

        mockMvc.perform(get("/v1/dashboard/resumo").param("idUnidade", idUnidade.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBoxes").value(8))
                .andExpect(jsonPath("$.taxaOcupacao").value(37.5))
                .andExpect(jsonPath("$.alugueisVigentes").value(3))
                .andExpect(jsonPath("$.clientesAtivos").value(2))
                .andExpect(jsonPath("$.receitaMensal").value(450.00));

        verify(dashboardService).resumo(idUnidade);
    }

    @Test
    @DisplayName("Should return status 400 when the idUnidade is not informed")
    void resumoWithoutIdUnidade() throws Exception {
        mockMvc.perform(get("/v1/dashboard/resumo"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return the ocupacao with status 200")
    void ocupacao() throws Exception {
        UUID idUnidade = UUID.randomUUID();

        DashboardOcupacaoDTO dto = DashboardOcupacaoDTO.builder().total(10).ocupados(4).bloqueados(1).livres(5).build();
        when(dashboardService.ocupacao(idUnidade)).thenReturn(dto);

        mockMvc.perform(get("/v1/dashboard/ocupacao").param("idUnidade", idUnidade.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.ocupados").value(4))
                .andExpect(jsonPath("$.bloqueados").value(1))
                .andExpect(jsonPath("$.livres").value(5));

        verify(dashboardService).ocupacao(idUnidade);
    }

    @Test
    @DisplayName("Should return the alugueis by status with status 200")
    void alugueisPorStatus() throws Exception {
        UUID idUnidade = UUID.randomUUID();

        when(dashboardService.alugueisPorStatus(idUnidade)).thenReturn(List.of(
                new DashboardAluguelStatusDTO(StatusAluguel.ATIVO, 5L)));

        mockMvc.perform(get("/v1/dashboard/alugueis-por-status").param("idUnidade", idUnidade.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ATIVO"))
                .andExpect(jsonPath("$[0].quantidade").value(5));

        verify(dashboardService).alugueisPorStatus(idUnidade);
    }

    @Test
    @DisplayName("Should return the evolucao dos alugueis with status 200")
    void evolucaoAlugueis() throws Exception {
        UUID idUnidade = UUID.randomUUID();

        when(dashboardService.evolucaoAlugueis(idUnidade)).thenReturn(List.of(
                new DashboardEvolucaoDTO(2026, 10, 2L, BigDecimal.valueOf(330.00))));

        mockMvc.perform(get("/v1/dashboard/evolucao-alugueis").param("idUnidade", idUnidade.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ano").value(2026))
                .andExpect(jsonPath("$[0].mes").value(10))
                .andExpect(jsonPath("$[0].quantidade").value(2))
                .andExpect(jsonPath("$[0].valor").value(330.00));

        verify(dashboardService).evolucaoAlugueis(idUnidade);
    }

    @Test
    @DisplayName("Should return the pendencias de assinatura with status 200")
    void pendenciasAssinatura() throws Exception {
        UUID idUnidade = UUID.randomUUID();
        UUID idAluguel = UUID.randomUUID();

        DashboardPendenciaDTO dto = DashboardPendenciaDTO.builder()
                .idAluguel(idAluguel).numeroBox("101").nomeCliente("Cliente 1")
                .status(StatusAluguel.PENDENTE_ASSINATURA_CONTRATO).valor(BigDecimal.valueOf(150.00)).build();
        when(dashboardService.pendenciasAssinatura(idUnidade)).thenReturn(List.of(dto));

        mockMvc.perform(get("/v1/dashboard/pendencias-assinatura").param("idUnidade", idUnidade.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAluguel").value(idAluguel.toString()))
                .andExpect(jsonPath("$[0].numeroBox").value("101"))
                .andExpect(jsonPath("$[0].nomeCliente").value("Cliente 1"))
                .andExpect(jsonPath("$[0].status").value("PENDENTE_ASSINATURA_CONTRATO"));

        verify(dashboardService).pendenciasAssinatura(idUnidade);
    }

}
