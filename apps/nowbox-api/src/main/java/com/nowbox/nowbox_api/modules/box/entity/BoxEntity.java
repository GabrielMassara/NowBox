package com.nowbox.nowbox_api.modules.box.entity;

import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_box")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 20)
    private String numero;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_unidade", nullable = false)
    @NotNull
    private UnidadeEntity unidade;

    @Column(precision = 8, scale = 2)
    private BigDecimal tamanho;

    @Column(length = 100)
    private String dimensoes;

    private Boolean disponivel;

    @Column(precision = 10, scale = 2)
    private BigDecimal preco;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}
