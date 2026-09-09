package com.nowbox.nowbox_api.modules.operacao.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OperacaoCreateDTO {
    private UUID idModulo;

    private String nome;

    private String codigo;
}
