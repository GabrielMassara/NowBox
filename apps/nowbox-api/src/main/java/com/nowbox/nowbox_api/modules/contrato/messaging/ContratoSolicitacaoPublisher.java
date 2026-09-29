package com.nowbox.nowbox_api.modules.contrato.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Envia a solicitacao somente depois do commit para o worker nunca processar um aluguel que sofreu rollback.
@Slf4j
@Component
@RequiredArgsConstructor
public class ContratoSolicitacaoPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publicar(ContratoSolicitadoMessage mensagem) {
        try {
            rabbitTemplate.convertAndSend(ContratoMessagingConfig.EXCHANGE, ContratoMessagingConfig.ROTA_SOLICITADO, mensagem);
        } catch (Exception e) {
            log.error("Nao foi possivel solicitar o contrato do aluguel {}", mensagem.idAluguel(), e);
        }
    }
}
