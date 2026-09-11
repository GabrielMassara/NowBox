package com.nowbox.nowbox_api.modules.usuario.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UsuarioFilterDTO {
    private String nome;

    private String email;

    private String cpf;
}
