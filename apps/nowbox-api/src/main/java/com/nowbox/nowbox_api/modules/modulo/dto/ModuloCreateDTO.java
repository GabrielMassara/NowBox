package com.nowbox.nowbox_api.modules.modulo.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ModuloCreateDTO {
    private UUID idSessao;

    private String nome;

    private String rota;
}
