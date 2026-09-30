package com.nowbox.nowbox_api.modules.email.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Envia a solicitacao somente depois do commit para nunca avisar o cliente de um aluguel que sofreu rollback
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSolicitacaoPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publicar(EmailSolicitadoMessage mensagem) {
        try {
            rabbitTemplate.convertAndSend(EmailMessagingConfig.EXCHANGE, EmailMessagingConfig.ROTA_ENVIAR, mensagem);
        } catch (Exception e) {
            log.error("Nao foi possivel solicitar o email {} ({})", mensagem.idEmail(), mensagem.template(), e);
        }
    }
}
