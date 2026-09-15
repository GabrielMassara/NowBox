package com.nowbox.nowbox_api.modules.cliente.dto;

import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ClienteResponseDTO {
    private UUID id;

    private EstadoEntity estado;

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

    private Boolean senhaTemporariaStatus;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;
}
