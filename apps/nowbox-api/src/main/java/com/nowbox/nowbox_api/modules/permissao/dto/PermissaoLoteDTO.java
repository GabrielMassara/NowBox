package com.nowbox.nowbox_api.modules.permissao.dto;

import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PermissaoLoteDTO {
    private Set<UUID> idsOperacao;
}
