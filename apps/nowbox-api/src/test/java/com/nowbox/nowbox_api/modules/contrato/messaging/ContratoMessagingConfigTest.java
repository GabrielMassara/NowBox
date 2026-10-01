package com.nowbox.nowbox_api.modules.contrato.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Garante que o JSON publicado continua compativel com o que o nowbox-jobs le
class ContratoMessagingConfigTest {

    private final MessageConverter converter = new ContratoMessagingConfig().messageConverter();

    @Test
    @DisplayName("Should write the solicitacao with the field names and formats the nowbox-jobs reads")
    void escreverSolicitacao() {
        ContratoSolicitadoMessage solicitacao = new ContratoSolicitadoMessage(UUID.randomUUID(), ContratoSolicitadoMessage.Tipo.ADITIVO, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "101", "12345678000199", new BigDecimal("1234.50"), LocalDate.of(2026, 9, 28), LocalDate.of(2026, 1, 10),
                List.of(new ContratoSolicitadoMessage.Alteracao("Valor", "R$ 100,00", "R$ 120,00")),
                new ContratoSolicitadoMessage.Contratante("Maria", "Engenheira", "12345678901", "MG1", "Rua 1", "10", null,
                        "Centro", "30123456", "BH", "MG", "3199", "m@e.com", true));

        String json = new String(converter.toMessage(solicitacao, new MessageProperties()).getBody(), StandardCharsets.UTF_8);

        assertThat(json).contains("\"idSolicitacao\"", "\"idAluguel\"", "\"idBox\"", "\"idUnidade\"", "\"numeroBox\":\"101\"",
                "\"cnpjLocadora\":\"12345678000199\"", "\"valor\":1234.50", "\"dataAssinatura\":\"2026-09-28\"", "\"tipo\":\"ADITIVO\"", "\"dataContratoOriginal\":\"2026-01-10\"",
                "\"alteracoes\":[{\"campo\":\"Valor\",\"valorAnterior\":\"R$ 100,00\",\"valorNovo\":\"R$ 120,00\"}]",
                "\"contratante\":{", "\"numeroEndereco\":\"10\"", "\"usarEnderecoParaCorrespondencia\":true");
    }

}
