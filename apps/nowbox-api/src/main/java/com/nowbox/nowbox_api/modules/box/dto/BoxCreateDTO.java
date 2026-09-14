package com.nowbox.nowbox_api.modules.box.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BoxCreateDTO {
    private UUID idUnidade;

    private String numero;

    private BigDecimal tamanho;

    private String dimensoes;

    private Boolean disponivel;

    private BigDecimal preco;
}
