package com.nowbox.nowbox_jobs.email;

import java.util.Map;
import java.util.UUID;

public record EmailSolicitadoMessage(
        UUID idEmail,
        String destinatario,
        String nomeDestinatario,
        String assunto,
        EmailTemplate template,
        Map<String, String> variaveis
) {
}
