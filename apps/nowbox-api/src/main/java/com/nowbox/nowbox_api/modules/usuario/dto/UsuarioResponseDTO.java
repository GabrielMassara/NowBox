package com.nowbox.nowbox_api.modules.usuario.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UsuarioResponseDTO {
    private UUID id;

    private String nome;

    private String email;

    private String cpf;

    private String sexo;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;
}
