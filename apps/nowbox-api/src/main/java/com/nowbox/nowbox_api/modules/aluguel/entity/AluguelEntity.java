package com.nowbox.nowbox_api.modules.aluguel.entity;

import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_aluguel")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AluguelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_box", nullable = false)
    @NotNull
    private BoxEntity box;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    @NotNull
    private ClienteEntity cliente;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    private StatusAluguel status;

    @OneToOne
    @JoinColumn(name = "id_arquivo_contrato")
    private ArquivoEntity contrato;

    @OneToOne
    @JoinColumn(name = "id_arquivo_distrato")
    private ArquivoEntity distrato;

    @OneToOne
    @JoinColumn(name = "id_arquivo_contrato_assinado")
    private ArquivoEntity contratoAssinado;

    @OneToOne
    @JoinColumn(name = "id_arquivo_distrato_assinado")
    private ArquivoEntity distratoAssinado;

    @ManyToOne
    @JoinColumn(name = "id_box_anterior")
    private BoxEntity boxAnterior;

    @ManyToOne
    @JoinColumn(name = "id_cliente_anterior")
    private ClienteEntity clienteAnterior;

    @Column(name = "valor_anterior", precision = 10, scale = 2)
    private BigDecimal valorAnterior;

    @Column(name = "observacao_anterior", columnDefinition = "TEXT")
    private String observacaoAnterior;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}
