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
public class BoxLoteDTO {
    private UUID idUnidade;

    private String prefixo;

    private Integer numeroInicial;

    private Integer numeroFinal;

    private Boolean completarComZeros;

    private BigDecimal tamanho;

    private String dimensoes;

    private Boolean disponivel;

    private BigDecimal preco;
}
