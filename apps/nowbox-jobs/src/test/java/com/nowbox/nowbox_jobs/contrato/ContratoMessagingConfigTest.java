package com.nowbox.nowbox_jobs.contrato;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContratoMessagingConfigTest {

    private final MessageConverter converter = new ContratoMessagingConfig().messageConverter();

    @Test
    @DisplayName("Should read the solicitacao published by the nowbox-api even though its class is in another package")
    void lerSolicitacaoDaApi() {
        UUID idSolicitacao = UUID.randomUUID();
        UUID idAluguel = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idUnidade = UUID.randomUUID();

        // JSON exatamente como a nowbox-api publica com o record ContratoSolicitadoMessage
        String json = """
                {"idSolicitacao":"%s","idAluguel":"%s","idBox":"%s","idUnidade":"%s","numeroBox":"101",
                 "cnpjLocadora":"12345678000199","valor":1234.50,"dataAssinatura":"2026-09-28",
                 "contratante":{"nome":"Maria","profissao":"Engenheira","cpf":"12345678901","rg":"MG1","endereco":"Rua 1",
                 "numeroEndereco":"10","complemento":null,"bairro":"Centro","cep":"30123456","cidade":"BH","uf":"MG",
                 "telefone":"3199","email":"m@e.com","usarEnderecoParaCorrespondencia":true}}
                """.formatted(idSolicitacao, idAluguel, idBox, idUnidade);

        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setHeader("__TypeId__", "com.nowbox.nowbox_api.modules.contrato.messaging.ContratoSolicitadoMessage");
        properties.setInferredArgumentType(ContratoSolicitadoMessage.class);

        Object lido = converter.fromMessage(new Message(json.getBytes(StandardCharsets.UTF_8), properties));

        assertThat(lido).isInstanceOf(ContratoSolicitadoMessage.class);
        ContratoSolicitadoMessage solicitacao = (ContratoSolicitadoMessage) lido;
        assertThat(solicitacao.idSolicitacao()).isEqualTo(idSolicitacao);
        assertThat(solicitacao.idAluguel()).isEqualTo(idAluguel);
        assertThat(solicitacao.idBox()).isEqualTo(idBox);
        assertThat(solicitacao.idUnidade()).isEqualTo(idUnidade);
        assertThat(solicitacao.numeroBox()).isEqualTo("101");
        assertThat(solicitacao.valor()).isEqualByComparingTo(new BigDecimal("1234.50"));
        assertThat(solicitacao.dataAssinatura()).isEqualTo(LocalDate.of(2026, 9, 28));
        assertThat(solicitacao.contratante().nome()).isEqualTo("Maria");
        assertThat(solicitacao.contratante().complemento()).isNull();
        assertThat(solicitacao.contratante().usarEnderecoParaCorrespondencia()).isTrue();
    }
}
