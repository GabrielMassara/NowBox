package com.nowbox.nowbox_api.modules.contrato.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AditivoResponseDTO {

    private UUID id;

    private UUID idAluguel;

    private UUID idBox;

    private String numeroBox;

    private String nomeCliente;

    private String nomeArquivo;

    private Long tamanho;

    private String descricao;

    private LocalDateTime salvoEm;

    private boolean pendenteAssinatura;

    private boolean assinado;

    private boolean cancelado;

}
