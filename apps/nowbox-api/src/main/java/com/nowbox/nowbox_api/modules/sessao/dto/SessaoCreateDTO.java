package com.nowbox.nowbox_api.modules.sessao.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SessaoCreateDTO {
    @NotBlank
    private String nome;
    @NotBlank
    private String rota;
}
