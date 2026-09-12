package com.nowbox.nowbox_api.modules.atribuicao.dto;

import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.usuario.entity.UsuarioEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AtribuicaoResponseDTO {

    private UUID id;

    private UsuarioEntity usuario;

    private CargoEntity cargo;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

}
