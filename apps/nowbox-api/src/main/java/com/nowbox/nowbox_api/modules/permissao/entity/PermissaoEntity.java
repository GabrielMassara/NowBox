package com.nowbox.nowbox_api.modules.permissao.entity;

import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_permissao")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PermissaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_cargo", nullable = false)
    @NotNull
    private CargoEntity cargo;

    @ManyToOne
    @JoinColumn(name = "id_operacao", nullable = false)
    @NotNull
    private OperacaoEntity operacao;

}
