package com.nowbox.nowbox_api.modules.contrato.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Isso tem que ser igual ao que tá declarado no nowbox-jobs. Se mudar algum argumento só de um lado, o RabbitMQ vai reclamar e rejeitar
@Configuration
public class ContratoMessagingConfig {

    public static final String EXCHANGE = "nowbox.contrato";
    public static final String DEAD_LETTER_EXCHANGE = "nowbox.contrato.dlx";

    public static final String FILA_SOLICITADO = "nowbox.contrato.solicitado";
    public static final String ROTA_SOLICITADO = "contrato.solicitado";

    @Bean
    public DirectExchange contratoExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange contratoDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue filaContratoSolicitado() {
        return QueueBuilder.durable(FILA_SOLICITADO)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(FILA_SOLICITADO + ".dlq")
                .build();
    }

    @Bean
    public Queue filaContratoSolicitadoDlq() {
        return QueueBuilder.durable(FILA_SOLICITADO + ".dlq").build();
    }

    @Bean
    public Binding bindingSolicitado(Queue filaContratoSolicitado, DirectExchange contratoExchange) {
        return BindingBuilder.bind(filaContratoSolicitado).to(contratoExchange).with(ROTA_SOLICITADO);
    }

    @Bean
    public Binding bindingSolicitadoDlq(Queue filaContratoSolicitadoDlq, DirectExchange contratoDeadLetterExchange) {
        return BindingBuilder.bind(filaContratoSolicitadoDlq).to(contratoDeadLetterExchange).with(FILA_SOLICITADO + ".dlq");
    }

    // A API so publica. O nowbox-jobs le o JSON pelo tipo do parametro do listener dele, entao os nomes dos campos e que precisam bater
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
