package com.nowbox.nowbox_api.modules.estado.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Table(name = "tb_estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class EstadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    private String nome;

    @Column(length = 2)
    private String uf;
}
