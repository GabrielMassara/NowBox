package com.nowbox.nowbox_api.modules.cliente.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ClienteFilterDTO {
    private UUID idEstado;

    private String nome;

    private String cpf;

    private String email;
}
