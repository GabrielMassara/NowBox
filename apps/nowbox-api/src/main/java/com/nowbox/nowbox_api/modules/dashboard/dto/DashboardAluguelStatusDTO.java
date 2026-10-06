package com.nowbox.nowbox_api.modules.dashboard.dto;

import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardAluguelStatusDTO {

    private StatusAluguel status;

    private long quantidade;

}
