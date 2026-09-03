package com.nowbox.nowbox_api.modules.sessao.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SessaoRequestFilterDTO {
    private String nome;
    private String rota;
}
