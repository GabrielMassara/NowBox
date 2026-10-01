package com.nowbox.nowbox_api.modules.cliente;

import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import jakarta.persistence.EntityManager;

import java.util.UUID;

// O documento de identidade e obrigatorio em tb_cliente, entao os testes de repository precisam de um arquivo persistido
public final class ClienteTestFixtures {

    private ClienteTestFixtures() {
    }

    public static ArquivoEntity documento(EntityManager em) {
        ArquivoEntity arquivo = ArquivoEntity.builder().bucket("nowbox-documentos").chave("clientes/documentos-identidade/" + UUID.randomUUID() + ".pdf")
                .nomeOriginal("rg.pdf").contentType("application/pdf").tamanho(100L).build();
        em.persist(arquivo);
        return arquivo;
    }
}
