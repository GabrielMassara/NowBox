package com.nowbox.nowbox_jobs.email;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    @DisplayName("Should build a multipart email with html, plain text and the embedded logo and hand it to the smtp server")
    void enviar() throws Exception {
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        EmailSender sender = new EmailSender(mailSender, new EmailTemplateRenderer());
        ReflectionTestUtils.setField(sender, "remetente", "contato@nowbox.com");
        ReflectionTestUtils.setField(sender, "nomeRemetente", "NowBox");

        sender.enviar(new EmailSolicitadoMessage(UUID.randomUUID(), "maria@email.com", "Maria", "Seu aluguel foi registrado com sucesso",
                EmailTemplate.ALUGUEL_REGISTRADO, Map.of("nome", "Maria", "numeroBox", "101")));

        ArgumentCaptor<MimeMessage> enviado = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(enviado.capture());

        MimeMessage mensagem = enviado.getValue();
        mensagem.saveChanges();
        assertThat(mensagem.getSubject()).isEqualTo("Seu aluguel foi registrado com sucesso");
        assertThat(mensagem.getFrom()[0].toString()).contains("contato@nowbox.com");
        assertThat(mensagem.getAllRecipients()[0].toString()).contains("maria@email.com");

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        mensagem.writeTo(saida);
        String bruto = saida.toString(StandardCharsets.US_ASCII);
        assertThat(bruto).contains("text/html", "text/plain", "image/png", "Content-ID: <" + EmailSender.CID_LOGO + ">");
        assertThat(MimeUtility.decodeText(mensagem.getSubject())).contains("registrado");
    }
}
