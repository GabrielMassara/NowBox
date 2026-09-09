package com.nowbox.nowbox_api.modules.operacao.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperacaoFilterDTO {

    private String nome;

    private String codigo;

    private UUID idModulo;

}
