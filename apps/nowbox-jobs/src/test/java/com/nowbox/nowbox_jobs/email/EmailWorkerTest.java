package com.nowbox.nowbox_jobs.email;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailWorkerTest {

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private EmailWorker worker;

    private final EmailSolicitadoMessage solicitacao = new EmailSolicitadoMessage(UUID.randomUUID(), "maria@email.com", "Maria", "Assunto",
            EmailTemplate.ALUGUEL_REGISTRADO, Map.of("nome", "Maria"));

    @Test
    @DisplayName("Should send the requested email")
    void processar() throws Exception {
        worker.processar(solicitacao);

        verify(emailSender).enviar(solicitacao);
    }

    @Test
    @DisplayName("Should let the failure go up so the listener retries and then sends the message to the dead letter queue")
    void falhaSobe() throws Exception {
        doThrow(new MessagingException("smtp fora do ar")).when(emailSender).enviar(solicitacao);

        assertThrows(MessagingException.class, () -> worker.processar(solicitacao));
    }
}
