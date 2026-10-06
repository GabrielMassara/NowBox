package com.nowbox.nowbox_api.modules.dashboard.service;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardAluguelStatusDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardEvolucaoDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardOcupacaoDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardPendenciaDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardResumoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private IBoxRepository boxRepository;

    @Mock
    private IAluguelRepository aluguelRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should return the resumo of the unidade")
    void resumoCase1() {
        UUID idUnidade = UUID.randomUUID();

        when(boxRepository.countByUnidadeIdAndDeletedAtIsNull(idUnidade)).thenReturn(8L);
        when(boxRepository.countOcupadosByUnidade(idUnidade)).thenReturn(3L);
        when(aluguelRepository.sumValorVigenteByUnidade(eq(idUnidade), any())).thenReturn(BigDecimal.valueOf(450.00));
        when(aluguelRepository.countByBoxUnidadeIdAndStatusInAndDeletedAtIsNull(eq(idUnidade), any())).thenReturn(3L);
        when(aluguelRepository.countClientesByUnidadeAndStatusIn(eq(idUnidade), any())).thenReturn(2L);

        DashboardResumoDTO result = dashboardService.resumo(idUnidade);

        assertThat(result.getTotalBoxes()).isEqualTo(8);
        assertThat(result.getTaxaOcupacao()).isEqualByComparingTo("37.5");
        assertThat(result.getAlugueisVigentes()).isEqualTo(3);
        assertThat(result.getClientesAtivos()).isEqualTo(2);
        assertThat(result.getReceitaMensal()).isEqualByComparingTo("450.00");
    }

    @Test
    @DisplayName("Should return zeros in the resumo when the unidade has no box and no aluguel")
    void resumoCase2() {
        UUID idUnidade = UUID.randomUUID();

        when(aluguelRepository.sumValorVigenteByUnidade(eq(idUnidade), any())).thenReturn(null);

        DashboardResumoDTO result = dashboardService.resumo(idUnidade);

        assertThat(result.getTotalBoxes()).isZero();
        assertThat(result.getTaxaOcupacao()).isEqualByComparingTo("0");
        assertThat(result.getReceitaMensal()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Should return the ocupacao with the livres as the rest of the boxes")
    void ocupacaoCase1() {
        UUID idUnidade = UUID.randomUUID();

        when(boxRepository.countByUnidadeIdAndDeletedAtIsNull(idUnidade)).thenReturn(10L);
        when(boxRepository.countOcupadosByUnidade(idUnidade)).thenReturn(4L);
        when(boxRepository.countBloqueadosNaoOcupadosByUnidade(idUnidade)).thenReturn(1L);

        DashboardOcupacaoDTO result = dashboardService.ocupacao(idUnidade);

        assertThat(result.getTotal()).isEqualTo(10);
        assertThat(result.getOcupados()).isEqualTo(4);
        assertThat(result.getBloqueados()).isEqualTo(1);
        assertThat(result.getLivres()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should return every status of aluguel, filling with zero the ones without aluguel")
    void alugueisPorStatusCase1() {
        UUID idUnidade = UUID.randomUUID();

        when(aluguelRepository.countByUnidadeGroupByStatus(idUnidade)).thenReturn(List.of(
                new DashboardAluguelStatusDTO(StatusAluguel.INATIVO, 2L),
                new DashboardAluguelStatusDTO(StatusAluguel.ATIVO, 5L)));

        List<DashboardAluguelStatusDTO> result = dashboardService.alugueisPorStatus(idUnidade);

        assertThat(result).extracting(DashboardAluguelStatusDTO::getStatus).containsExactly(StatusAluguel.values());
        assertThat(result).extracting(DashboardAluguelStatusDTO::getQuantidade).containsExactly(0L, 5L, 0L, 0L, 2L);
    }

    @Test
    @DisplayName("Should return the last six months of evolucao, filling with zero the months without aluguel")
    void evolucaoAlugueisCase1() {
        UUID idUnidade = UUID.randomUUID();
        YearMonth atual = YearMonth.now();

        when(aluguelRepository.countByUnidadeGroupByMes(eq(idUnidade), any())).thenReturn(List.of(
                new DashboardEvolucaoDTO(atual.getYear(), atual.getMonthValue(), 2L, BigDecimal.valueOf(330.00))));

        List<DashboardEvolucaoDTO> result = dashboardService.evolucaoAlugueis(idUnidade);

        assertThat(result).hasSize(6);

        DashboardEvolucaoDTO primeiro = result.getFirst();
        YearMonth inicio = atual.minusMonths(5);
        assertThat(primeiro.getAno()).isEqualTo(inicio.getYear());
        assertThat(primeiro.getMes()).isEqualTo(inicio.getMonthValue());
        assertThat(primeiro.getQuantidade()).isZero();
        assertThat(primeiro.getValor()).isEqualByComparingTo("0");

        DashboardEvolucaoDTO ultimo = result.getLast();
        assertThat(ultimo.getAno()).isEqualTo(atual.getYear());
        assertThat(ultimo.getMes()).isEqualTo(atual.getMonthValue());
        assertThat(ultimo.getQuantidade()).isEqualTo(2);
        assertThat(ultimo.getValor()).isEqualByComparingTo("330.00");
    }

    @Test
    @DisplayName("Should list the pendencias de assinatura of the unidade")
    void pendenciasAssinaturaCase1() {
        UUID idUnidade = UUID.randomUUID();
        LocalDateTime criadoEm = LocalDateTime.now().minusDays(3);

        AluguelEntity aluguel = AluguelEntity.builder()
                .id(UUID.randomUUID())
                .box(BoxEntity.builder().numero("101").build())
                .cliente(ClienteEntity.builder().nome("Cliente 1").build())
                .valor(BigDecimal.valueOf(150.00))
                .status(StatusAluguel.PENDENTE_ASSINATURA_CONTRATO)
                .createdAt(criadoEm)
                .build();
        when(aluguelRepository.findAllByUnidadeAndStatusIn(eq(idUnidade), any(), any())).thenReturn(List.of(aluguel));

        List<DashboardPendenciaDTO> result = dashboardService.pendenciasAssinatura(idUnidade);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getIdAluguel()).isEqualTo(aluguel.getId());
        assertThat(result.getFirst().getNumeroBox()).isEqualTo("101");
        assertThat(result.getFirst().getNomeCliente()).isEqualTo("Cliente 1");
        assertThat(result.getFirst().getStatus()).isEqualTo(StatusAluguel.PENDENTE_ASSINATURA_CONTRATO);
        assertThat(result.getFirst().getValor()).isEqualByComparingTo("150.00");
        assertThat(result.getFirst().getCreatedAt()).isEqualTo(criadoEm);
    }

}
