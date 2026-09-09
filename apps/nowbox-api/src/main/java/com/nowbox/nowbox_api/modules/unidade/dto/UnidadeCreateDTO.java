package com.nowbox.nowbox_api.modules.unidade.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UnidadeCreateDTO {
    private UUID idEstado;

    private String nome;

    private String cnpj;

    private String endereco;

    private String numero;

    private String complemento;

    private String bairro;

    private String cep;

    private String cidade;
}
