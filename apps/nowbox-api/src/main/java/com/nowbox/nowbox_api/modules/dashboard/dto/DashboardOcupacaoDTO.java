package com.nowbox.nowbox_api.modules.dashboard.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardOcupacaoDTO {

    private long total;

    private long ocupados;

    private long bloqueados;

    private long livres;

}
