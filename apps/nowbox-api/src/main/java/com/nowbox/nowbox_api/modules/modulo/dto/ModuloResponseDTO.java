package com.nowbox.nowbox_api.modules.modulo.dto;

import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ModuloResponseDTO {
    private UUID id;

    private SessaoEntity sessao;

    private String nome;

    private String rota;
}
