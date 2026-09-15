package com.nowbox.nowbox_api.modules.cliente.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ClienteCreateDTO {
    private UUID idEstado;

    private String nome;

    private String profissao;

    private String cpf;

    private String rg;

    private String email;

    private String telefone;

    private String sexo;

    private LocalDate nascimento;

    private String endereco;

    private String numero;

    private String complemento;

    private String bairro;

    private String cep;

    private String cidade;

    private Boolean enderecoCorrespondencia;

    private String senha;

    private String senhaTemporaria;

    private Boolean senhaTemporariaStatus;
}
