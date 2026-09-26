package com.nowbox.nowbox_api.modules.box.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BoxFilterDTO {
    private UUID idUnidade;

    private String numero;

    private Boolean disponivel;

    private Boolean alugado;
}
