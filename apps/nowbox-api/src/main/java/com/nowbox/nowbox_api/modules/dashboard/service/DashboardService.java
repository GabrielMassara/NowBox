package com.nowbox.nowbox_api.modules.dashboard.service;

import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardAluguelStatusDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardEvolucaoDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardOcupacaoDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardPendenciaDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardResumoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int MESES_EVOLUCAO = 6;
    private static final int LIMITE_PENDENCIAS = 10;

    private static final List<StatusAluguel> STATUS_VIGENTES = List.of(
            StatusAluguel.ATIVO,
            StatusAluguel.PENDENTE_ASSINATURA_ADITIVO,
            StatusAluguel.PENDENTE_ASSINATURA_DISTRATO);

    private static final List<StatusAluguel> STATUS_PENDENTES = List.of(
            StatusAluguel.PENDENTE_ASSINATURA_CONTRATO,
            StatusAluguel.PENDENTE_ASSINATURA_ADITIVO,
            StatusAluguel.PENDENTE_ASSINATURA_DISTRATO);

    private final IBoxRepository boxRepository;
    private final IAluguelRepository aluguelRepository;

    public DashboardResumoDTO resumo(UUID idUnidade) {
        long totalBoxes = boxRepository.countByUnidadeIdAndDeletedAtIsNull(idUnidade);
        long ocupados = boxRepository.countOcupadosByUnidade(idUnidade);

        BigDecimal taxaOcupacao = totalBoxes == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(ocupados * 100).divide(BigDecimal.valueOf(totalBoxes), 1, RoundingMode.HALF_UP);

        BigDecimal receitaMensal = aluguelRepository.sumValorVigenteByUnidade(idUnidade, STATUS_VIGENTES);

        return DashboardResumoDTO.builder()
                .totalBoxes(totalBoxes)
                .taxaOcupacao(taxaOcupacao)
                .alugueisVigentes(aluguelRepository.countByBoxUnidadeIdAndStatusInAndDeletedAtIsNull(idUnidade, STATUS_VIGENTES))
                .clientesAtivos(aluguelRepository.countClientesByUnidadeAndStatusIn(idUnidade, STATUS_VIGENTES))
                .receitaMensal(receitaMensal == null ? BigDecimal.ZERO : receitaMensal)
                .build();
    }

    public DashboardOcupacaoDTO ocupacao(UUID idUnidade) {
        long total = boxRepository.countByUnidadeIdAndDeletedAtIsNull(idUnidade);
        long ocupados = boxRepository.countOcupadosByUnidade(idUnidade);
        long bloqueados = boxRepository.countBloqueadosNaoOcupadosByUnidade(idUnidade);

        return DashboardOcupacaoDTO.builder()
                .total(total)
                .ocupados(ocupados)
                .bloqueados(bloqueados)
                .livres(total - ocupados - bloqueados)
                .build();
    }

    public List<DashboardAluguelStatusDTO> alugueisPorStatus(UUID idUnidade) {
        Map<StatusAluguel, Long> quantidades = new EnumMap<>(StatusAluguel.class);
        aluguelRepository.countByUnidadeGroupByStatus(idUnidade)
                .forEach(item -> quantidades.put(item.getStatus(), item.getQuantidade()));

        return Arrays.stream(StatusAluguel.values())
                .map(status -> new DashboardAluguelStatusDTO(status, quantidades.getOrDefault(status, 0L)))
                .toList();
    }

    public List<DashboardEvolucaoDTO> evolucaoAlugueis(UUID idUnidade) {
        YearMonth atual = YearMonth.now();
        YearMonth inicio = atual.minusMonths(MESES_EVOLUCAO - 1L);

        Map<YearMonth, DashboardEvolucaoDTO> registrados = new HashMap<>();
        aluguelRepository.countByUnidadeGroupByMes(idUnidade, inicio.atDay(1).atStartOfDay())
                .forEach(item -> registrados.put(YearMonth.of(item.getAno(), item.getMes()), item));

        List<DashboardEvolucaoDTO> evolucao = new ArrayList<>();
        for (YearMonth mes = inicio; !mes.isAfter(atual); mes = mes.plusMonths(1)) {
            DashboardEvolucaoDTO registrado = registrados.get(mes);

            evolucao.add(DashboardEvolucaoDTO.builder()
                    .ano(mes.getYear())
                    .mes(mes.getMonthValue())
                    .quantidade(registrado == null ? 0 : registrado.getQuantidade())
                    .valor(registrado == null || registrado.getValor() == null ? BigDecimal.ZERO : registrado.getValor())
                    .build());
        }

        return evolucao;
    }

    public List<DashboardPendenciaDTO> pendenciasAssinatura(UUID idUnidade) {
        return aluguelRepository.findAllByUnidadeAndStatusIn(idUnidade, STATUS_PENDENTES, PageRequest.of(0, LIMITE_PENDENCIAS))
                .stream()
                .map(aluguel -> DashboardPendenciaDTO.builder()
                        .idAluguel(aluguel.getId())
                        .numeroBox(aluguel.getBox().getNumero())
                        .nomeCliente(aluguel.getCliente().getNome())
                        .status(aluguel.getStatus())
                        .valor(aluguel.getValor())
                        .createdAt(aluguel.getCreatedAt())
                        .build())
                .toList();
    }

}
