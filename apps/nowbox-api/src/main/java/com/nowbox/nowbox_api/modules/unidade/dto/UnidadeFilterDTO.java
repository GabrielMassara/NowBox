package com.nowbox.nowbox_api.modules.unidade.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UnidadeFilterDTO {
    private UUID idEstado;

    private String nome;

    private String cnpj;

    private String cidade;
}
