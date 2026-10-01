package com.nowbox.nowbox_api.modules.email.messaging;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.contrato.messaging.ContratoMessagingConfig;
import com.nowbox.nowbox_api.modules.contrato.messaging.ContratoSolicitadoMessage;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Garante que o JSON publicado continua compativel com o que o nowbox-jobs le
class EmailMessagingConfigTest {

    private final MessageConverter converter = new ContratoMessagingConfig().messageConverter();

    @Test
    @DisplayName("Should write the email solicitacao with the field names and formats the nowbox-jobs reads")
    void escreverSolicitacao() {
        EmailSolicitadoMessage solicitacao = new EmailSolicitadoMessage(UUID.randomUUID(), "m@e.com", "Maria", "Assunto",
                EmailSolicitadoMessage.Template.ALUGUEL_REGISTRADO, Map.of("numeroBox", "101"));

        String json = new String(converter.toMessage(solicitacao, new MessageProperties()).getBody(), StandardCharsets.UTF_8);

        assertThat(json).contains("\"idEmail\"", "\"destinatario\":\"m@e.com\"", "\"nomeDestinatario\":\"Maria\"", "\"assunto\":\"Assunto\"",
                "\"template\":\"ALUGUEL_REGISTRADO\"", "\"variaveis\":{\"numeroBox\":\"101\"}");
    }

    @Test
    @DisplayName("Should build the aluguel registered email with the aluguel data formatted for the template")
    void montarAluguelRegistrado() {
        BoxEntity box = BoxEntity.builder().numero("101").unidade(UnidadeEntity.builder().nome("Unidade Centro").build()).build();
        ClienteEntity cliente = ClienteEntity.builder().nome("Maria").email("m@e.com").build();
        AluguelEntity aluguel = AluguelEntity.builder().box(box).cliente(cliente).valor(new BigDecimal("1234.50")).build();

        EmailSolicitadoMessage email = EmailSolicitadoMessage.aluguelRegistrado(aluguel);

        assertThat(email.destinatario()).isEqualTo("m@e.com");
        assertThat(email.template()).isEqualTo(EmailSolicitadoMessage.Template.ALUGUEL_REGISTRADO);
        assertThat(email.variaveis()).containsEntry("nome", "Maria").containsEntry("numeroBox", "101").containsEntry("unidade", "Unidade Centro");
        assertThat(email.variaveis().get("valor").replace(' ', ' ')).isEqualTo("R$ 1.234,50");
        assertThat(email.variaveis().get("data")).matches("\\d{2}/\\d{2}/\\d{4}");
    }

    @Test
    @DisplayName("Should list the changes of the aluguel in the aluguel changed email")
    void montarAluguelAlterado() {
        BoxEntity box = BoxEntity.builder().numero("101").unidade(UnidadeEntity.builder().nome("Unidade Centro").build()).build();
        ClienteEntity cliente = ClienteEntity.builder().nome("Maria").email("m@e.com").build();
        AluguelEntity aluguel = AluguelEntity.builder().box(box).cliente(cliente).valor(new BigDecimal("200.00")).build();

        EmailSolicitadoMessage email = EmailSolicitadoMessage.aluguelAlterado(aluguel, List.of(
                new ContratoSolicitadoMessage.Alteracao("Valor", "R$ 150,00", "R$ 200,00"),
                new ContratoSolicitadoMessage.Alteracao("Situação", "Ativo", "Inativo")));

        assertThat(email.template()).isEqualTo(EmailSolicitadoMessage.Template.ALUGUEL_ALTERADO);
        assertThat(email.variaveis().get("alteracoes")).isEqualTo("Valor: R$ 150,00 → R$ 200,00; Situação: Ativo → Inativo");
    }
}
