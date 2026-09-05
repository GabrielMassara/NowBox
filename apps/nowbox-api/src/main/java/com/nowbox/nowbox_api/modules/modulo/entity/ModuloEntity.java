package com.nowbox.nowbox_api.modules.modulo.entity;

import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Table(name = "tb_modulo")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ModuloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_sessao", nullable = false)
    @NotNull
    private SessaoEntity sessao;

    @Column(length = 100)
    private String nome;

    @Column(length = 200)
    private String rota;

}
