package com.nowbox.nowbox_api.modules.contrato.entity;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

// Vincula um aditivo de contrato ao aluguel
// O box e gravado junto porque o aluguel pode trocar de box, e o historico do box deve refletir o box do aditivo
@Entity
@Table(name = "tb_arquivo_aluguel")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArquivoAluguelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_arquivo", nullable = false)
    @NotNull
    private ArquivoEntity arquivo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_aluguel", nullable = false)
    @NotNull
    private AluguelEntity aluguel;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_box", nullable = false)
    @NotNull
    private BoxEntity box;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "pendente_assinatura")
    private boolean pendenteAssinatura;

    private boolean cancelado;

    @OneToOne
    @JoinColumn(name = "id_arquivo_assinado")
    private ArquivoEntity assinado;

    @Column(name = "salvo_em")
    private LocalDateTime salvoEm;

}
