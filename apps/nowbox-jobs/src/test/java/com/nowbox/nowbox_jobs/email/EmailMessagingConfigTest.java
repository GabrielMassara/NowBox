package com.nowbox.nowbox_jobs.email;

import com.nowbox.nowbox_jobs.contrato.ContratoMessagingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailMessagingConfigTest {

    private final MessageConverter converter = new ContratoMessagingConfig().messageConverter();

    @Test
    @DisplayName("Should read the email solicitacao published by the nowbox-api even though its class is in another package")
    void lerSolicitacaoDaApi() {
        UUID idEmail = UUID.randomUUID();

        String json = """
                {"idEmail":"%s","destinatario":"m@e.com","nomeDestinatario":"Maria","assunto":"Assunto",
                 "template":"ALUGUEL_REGISTRADO","variaveis":{"nome":"Maria","numeroBox":"101"}}
                """.formatted(idEmail);

        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setHeader("__TypeId__", "com.nowbox.nowbox_api.modules.email.messaging.EmailSolicitadoMessage");
        properties.setInferredArgumentType(EmailSolicitadoMessage.class);

        Object lido = converter.fromMessage(new Message(json.getBytes(StandardCharsets.UTF_8), properties));

        assertThat(lido).isInstanceOf(EmailSolicitadoMessage.class);
        EmailSolicitadoMessage solicitacao = (EmailSolicitadoMessage) lido;
        assertThat(solicitacao.idEmail()).isEqualTo(idEmail);
        assertThat(solicitacao.destinatario()).isEqualTo("m@e.com");
        assertThat(solicitacao.nomeDestinatario()).isEqualTo("Maria");
        assertThat(solicitacao.assunto()).isEqualTo("Assunto");
        assertThat(solicitacao.template()).isEqualTo(EmailTemplate.ALUGUEL_REGISTRADO);
        assertThat(solicitacao.variaveis()).containsEntry("nome", "Maria").containsEntry("numeroBox", "101");
    }
}
