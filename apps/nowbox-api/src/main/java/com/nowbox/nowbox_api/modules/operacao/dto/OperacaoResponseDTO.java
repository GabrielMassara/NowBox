package com.nowbox.nowbox_api.modules.operacao.dto;

import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperacaoResponseDTO {

    private UUID id;

    private String nome;

    private String codigo;

    private ModuloEntity modulo;

}
