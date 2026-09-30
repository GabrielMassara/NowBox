package com.nowbox.nowbox_api.modules.email.messaging;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

// Solicitacao de envio de email ao worker. O template fica no worker, aqui vai so o nome dele e os valores ja formatados
public record EmailSolicitadoMessage(
        UUID idEmail,
        String destinatario,
        String nomeDestinatario,
        String assunto,
        Template template,
        Map<String, String> variaveis
) {

    // Os nomes precisam existir no enum EmailTemplate do nowbox-jobs
    public enum Template {
        ALUGUEL_REGISTRADO
    }

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);

    public static EmailSolicitadoMessage aluguelRegistrado(AluguelEntity aluguel) {
        BoxEntity box = aluguel.getBox();
        ClienteEntity cliente = aluguel.getCliente();

        Map<String, String> variaveis = new LinkedHashMap<>();
        variaveis.put("nome", cliente.getNome());
        variaveis.put("numeroBox", box.getNumero());
        variaveis.put("unidade", box.getUnidade() != null ? box.getUnidade().getNome() : null);
        variaveis.put("valor", aluguel.getValor() != null ? NumberFormat.getCurrencyInstance(PT_BR).format(aluguel.getValor()) : null);
        variaveis.put("data", DATA.format(LocalDate.now()));

        return new EmailSolicitadoMessage(
                UUID.randomUUID(),
                cliente.getEmail().trim(),
                cliente.getNome(),
                "Seu aluguel foi registrado com sucesso",
                Template.ALUGUEL_REGISTRADO,
                variaveis
        );
    }
}
