package com.nowbox.nowbox_api.modules.cliente.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "tb_cliente")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 150)
    private String nome;

    @Column(length = 100)
    private String profissao;

    @Column(length = 11)
    private String cpf;

    @Column(length = 20)
    private String rg;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 1)
    private String sexo;

    private LocalDate nascimento;

    @Column(length = 200)
    private String endereco;

    @Column(length = 10)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String bairro;

    @Column(length = 8)
    private String cep;

    @Column(length = 100)
    private String cidade;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_estado", nullable = false)
    @NotNull
    private EstadoEntity estado;

    @Column(name = "endereco_correspondencia")
    private Boolean enderecoCorrespondencia;

    @JsonIgnore
    @Column(length = 60)
    private String senha;

    @JsonIgnore
    @Column(name = "senha_temporaria", length = 60)
    private String senhaTemporaria;

    @Column(name = "senha_temporaria_status")
    private Boolean senhaTemporariaStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}
