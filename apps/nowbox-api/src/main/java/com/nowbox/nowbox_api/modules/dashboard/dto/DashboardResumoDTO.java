package com.nowbox.nowbox_api.modules.dashboard.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardResumoDTO {

    private long totalBoxes;

    private BigDecimal taxaOcupacao;

    private long alugueisVigentes;

    private long clientesAtivos;

    private BigDecimal receitaMensal;

}
