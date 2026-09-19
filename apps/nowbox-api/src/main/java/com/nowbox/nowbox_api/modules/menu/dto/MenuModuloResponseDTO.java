package com.nowbox.nowbox_api.modules.menu.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class MenuModuloResponseDTO {
    private UUID id;
    private String nome;
    private String rota;
}
