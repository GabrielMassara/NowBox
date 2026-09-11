package com.nowbox.nowbox_api.modules.permissao.dto;

import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissaoResponseDTO {

    private UUID id;

    private CargoEntity cargo;

    private OperacaoEntity operacao;

}
