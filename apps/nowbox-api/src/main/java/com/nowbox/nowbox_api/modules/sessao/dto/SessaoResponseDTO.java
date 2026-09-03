package com.nowbox.nowbox_api.modules.sessao.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SessaoResponseDTO {
    private UUID id;
    private String nome;
    private String rota;
}
