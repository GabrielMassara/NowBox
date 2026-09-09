package com.nowbox.nowbox_api.modules.unidade.dto;

import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UnidadeResponseDTO {
    private UUID id;

    private EstadoEntity estado;

    private String nome;

    private String cnpj;

    private String endereco;

    private String numero;

    private String complemento;

    private String bairro;

    private String cep;

    private String cidade;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;
}
