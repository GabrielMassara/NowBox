package com.nowbox.nowbox_api.modules.cliente.entity;

import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_arquivo_cliente")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArquivoClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_arquivo", nullable = false)
    @NotNull
    private ArquivoEntity arquivo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    @NotNull
    private ClienteEntity cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull
    private TipoDocumentoCliente tipo;

    @Column(name = "salvo_em")
    private LocalDateTime salvoEm;

}
