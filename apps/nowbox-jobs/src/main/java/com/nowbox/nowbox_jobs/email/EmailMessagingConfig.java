package com.nowbox.nowbox_jobs.email;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// A topologia abaixo precisa ser identica a declarada na nowbox-api pq o RabbitMQ rejeita redeclaracoes com argumentos diferentes
// O conversor JSON é o mesmo declarado em ContratoMessagingConfig
@Configuration
public class EmailMessagingConfig {

    public static final String EXCHANGE = "nowbox.email";
    public static final String DEAD_LETTER_EXCHANGE = "nowbox.email.dlx";

    public static final String FILA_ENVIAR = "nowbox.email.enviar";
    public static final String ROTA_ENVIAR = "email.enviar";

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange emailDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue filaEmailEnviar() {
        return QueueBuilder.durable(FILA_ENVIAR)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(FILA_ENVIAR + ".dlq")
                .build();
    }

    @Bean
    public Queue filaEmailEnviarDlq() {
        return QueueBuilder.durable(FILA_ENVIAR + ".dlq").build();
    }

    @Bean
    public Binding bindingEmailEnviar(Queue filaEmailEnviar, DirectExchange emailExchange) {
        return BindingBuilder.bind(filaEmailEnviar).to(emailExchange).with(ROTA_ENVIAR);
    }

    @Bean
    public Binding bindingEmailEnviarDlq(Queue filaEmailEnviarDlq, DirectExchange emailDeadLetterExchange) {
        return BindingBuilder.bind(filaEmailEnviarDlq).to(emailDeadLetterExchange).with(FILA_ENVIAR + ".dlq");
    }
}
