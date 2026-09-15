package com.nowbox.nowbox_api.modules.aluguel.dto;

import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AluguelResponseDTO {

    private UUID id;

    private BoxEntity box;

    private ClienteEntity cliente;

    private BigDecimal valor;

    private String observacao;

    private Boolean status;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

}
