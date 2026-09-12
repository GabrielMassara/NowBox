package com.nowbox.nowbox_api.modules.atribuicao.entity;

import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.usuario.entity.UsuarioEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_atribuicao")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AtribuicaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @NotNull
    private UsuarioEntity usuario;

    @ManyToOne
    @JoinColumn(name = "id_cargo", nullable = false)
    @NotNull
    private CargoEntity cargo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}
