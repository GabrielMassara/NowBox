package com.nowbox.nowbox_api.modules.atribuicao.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class AtribuicaoCreateDTO {
    private UUID idUsuario;

    private UUID idCargo;
}
