package com.nowbox.nowbox_api.modules.cargo.dto;

import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CargoResponseDTO {

    private UUID id;

    private String nome;

    private UnidadeEntity unidade;

}
