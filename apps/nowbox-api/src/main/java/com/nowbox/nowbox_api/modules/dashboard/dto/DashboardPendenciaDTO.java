package com.nowbox.nowbox_api.modules.dashboard.dto;

import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardPendenciaDTO {

    private UUID idAluguel;

    private String numeroBox;

    private String nomeCliente;

    private StatusAluguel status;

    private BigDecimal valor;

    private LocalDateTime createdAt;

}
