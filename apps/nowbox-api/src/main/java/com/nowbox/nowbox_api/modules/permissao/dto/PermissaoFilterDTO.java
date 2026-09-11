package com.nowbox.nowbox_api.modules.permissao.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissaoFilterDTO {

    private UUID idCargo;

    private UUID idOperacao;

}
