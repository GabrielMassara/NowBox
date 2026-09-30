package com.nowbox.nowbox_jobs.email;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// Worker dedicado ao envio de emails. Falhas sobem como excecao para o retry do listener e depois de estourar as tentativas a mensagem vai para a DLQ
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailWorker {

    private final EmailSender emailSender;

    @RabbitListener(queues = EmailMessagingConfig.FILA_ENVIAR)
    public void processar(EmailSolicitadoMessage solicitacao) throws MessagingException {
        log.info("Enviando email {} ({}) para {}", solicitacao.idEmail(), solicitacao.template(), solicitacao.destinatario());

        emailSender.enviar(solicitacao);

        log.info("Email {} enviado para {}", solicitacao.idEmail(), solicitacao.destinatario());
    }
}
