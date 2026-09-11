package com.nowbox.nowbox_api.modules.cargo.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CargoCreateDTO {
    private UUID idUnidade;

    private String nome;
}
