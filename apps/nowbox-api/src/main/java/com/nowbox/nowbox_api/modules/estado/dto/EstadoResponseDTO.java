package com.nowbox.nowbox_api.modules.estado.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class EstadoResponseDTO {
    private UUID id;

    private String nome;

    private String uf;
}
