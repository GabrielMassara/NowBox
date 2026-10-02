package com.nowbox.nowbox_api.modules.aluguel.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class AluguelCreateDTO {
    private UUID idBox;

    private UUID idCliente;

    private BigDecimal valor;

    private String observacao;
}
