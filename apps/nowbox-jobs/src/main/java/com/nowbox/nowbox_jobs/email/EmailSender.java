package com.nowbox.nowbox_jobs.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Monta o email e entrega ao servidor SMTP
@Component
@RequiredArgsConstructor
public class EmailSender {

    static final String CID_LOGO = "logo";

    private final JavaMailSender mailSender;
    private final EmailTemplateRenderer renderer;

    @Value("${app.mail.from}")
    private String remetente;

    @Value("${app.mail.from-name:NowBox}")
    private String nomeRemetente;

    public void enviar(EmailSolicitadoMessage solicitacao) throws MessagingException {
        MimeMessage mensagem = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, MimeMessageHelper.MULTIPART_MODE_RELATED, StandardCharsets.UTF_8.name());

        try {
            helper.setFrom(new InternetAddress(remetente, nomeRemetente, StandardCharsets.UTF_8.name()));
            helper.setTo(new InternetAddress(solicitacao.destinatario(), solicitacao.nomeDestinatario(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Encoding invalido no endereco do email", e);
        }

        helper.setSubject(solicitacao.assunto());
        helper.setText(renderer.renderizarTexto(solicitacao.template(), solicitacao.variaveis()),
                renderer.renderizarHtml(solicitacao.template(), solicitacao.variaveis()));
        helper.addInline(CID_LOGO, new ClassPathResource("email/logo.png"), "image/png");

        mailSender.send(mensagem);
    }
}
