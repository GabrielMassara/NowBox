package com.nowbox.nowbox_api.modules.aluguel.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AluguelFilterDTO {

    private UUID idBox;

    private UUID idCliente;

    private Boolean status;

}
