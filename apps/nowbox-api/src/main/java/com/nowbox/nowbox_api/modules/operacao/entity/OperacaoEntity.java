package com.nowbox.nowbox_api.modules.operacao.entity;

import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_operacao")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OperacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    private String nome;

    @Column(length = 100)
    private String codigo;

    @ManyToOne
    @JoinColumn(name = "id_modulo", nullable = false)
    @NotNull
    private ModuloEntity modulo;

}
