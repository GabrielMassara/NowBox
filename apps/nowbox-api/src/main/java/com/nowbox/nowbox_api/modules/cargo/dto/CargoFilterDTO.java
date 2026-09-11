package com.nowbox.nowbox_api.modules.cargo.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CargoFilterDTO {

    private String nome;

    private UUID idUnidade;

}
