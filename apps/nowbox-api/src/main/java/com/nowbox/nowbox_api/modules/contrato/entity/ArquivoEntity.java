package com.nowbox.nowbox_api.modules.contrato.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

// Referencia de um arquivo guardado no MinIO
@Entity
@Table(name = "tb_arquivo")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArquivoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    private String bucket;

    @Column(length = 500)
    private String chave;

    @Column(name = "nome_original", length = 200)
    private String nomeOriginal;

    @Column(name = "content_type", length = 100)
    private String contentType;

    private Long tamanho;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
