package com.nowbox.nowbox_api.modules.dashboard.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardEvolucaoDTO {

    private int ano;

    private int mes;

    private long quantidade;

    private BigDecimal valor;

}
