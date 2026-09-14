package com.nowbox.nowbox_api.modules.box.dto;

import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BoxResponseDTO {
    private UUID id;

    private UnidadeEntity unidade;

    private String numero;

    private BigDecimal tamanho;

    private String dimensoes;

    private Boolean disponivel;

    private BigDecimal preco;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;
}
