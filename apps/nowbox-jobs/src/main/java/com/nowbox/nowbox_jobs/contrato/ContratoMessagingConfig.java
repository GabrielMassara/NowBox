package com.nowbox.nowbox_jobs.contrato;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// A topologia abaixo precisa ser identica a declarada na nowbox-api pq o RabbitMQ rejeita redeclaracoes com argumentos diferentes
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

    @Bean
    public MessageConverter messageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
