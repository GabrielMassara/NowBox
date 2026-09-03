package com.nowbox.nowbox_api.modules.sessao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.UUID;

@Table(name = "tb_sessao")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    @NotBlank
    private String nome;

    @Column(length = 100)
    @NotBlank
    private String rota;

}
