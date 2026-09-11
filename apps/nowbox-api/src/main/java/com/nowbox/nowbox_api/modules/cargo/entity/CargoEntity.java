package com.nowbox.nowbox_api.modules.cargo.entity;

import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_cargo")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CargoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "id_unidade", nullable = false)
    @NotNull
    private UnidadeEntity unidade;

}
