package com.nowbox.nowbox_api.modules.email.messaging;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.contrato.messaging.ContratoSolicitadoMessage;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
        ALUGUEL_REGISTRADO,
        ALUGUEL_ALTERADO,
        ALUGUEL_ENCERRADO
    }

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);

    public static EmailSolicitadoMessage aluguelRegistrado(AluguelEntity aluguel) {
        return de(aluguel, "Seu aluguel foi registrado com sucesso", Template.ALUGUEL_REGISTRADO);
    }

    public static EmailSolicitadoMessage aluguelAlterado(AluguelEntity aluguel, List<ContratoSolicitadoMessage.Alteracao> alteracoes) {
        EmailSolicitadoMessage mensagem = de(aluguel, "Um aditivo foi feito no seu contrato", Template.ALUGUEL_ALTERADO);
        mensagem.variaveis().put("alteracoes", alteracoes.stream()
                .map(a -> a.campo() + ": " + a.valorAnterior() + " → " + a.valorNovo())
                .collect(Collectors.joining("; ")));
        return mensagem;
    }

    public static EmailSolicitadoMessage aluguelEncerrado(AluguelEntity aluguel) {
        return de(aluguel, "Seu contrato de aluguel foi encerrado", Template.ALUGUEL_ENCERRADO);
    }

    private static EmailSolicitadoMessage de(AluguelEntity aluguel, String assunto, Template template) {
        BoxEntity box = aluguel.getBox();
        ClienteEntity cliente = aluguel.getCliente();

        Map<String, String> variaveis = new LinkedHashMap<>();
        variaveis.put("nome", cliente.getNome());
        variaveis.put("numeroBox", box.getNumero());
        variaveis.put("unidade", box.getUnidade() != null ? box.getUnidade().getNome() : null);
        variaveis.put("valor", aluguel.getValor() != null ? NumberFormat.getCurrencyInstance(PT_BR).format(aluguel.getValor()) : null);
        variaveis.put("situacao", aluguel.getStatus() == StatusAluguel.ATIVO ? "Ativo" : "Inativo");
        variaveis.put("data", DATA.format(LocalDate.now()));

        return new EmailSolicitadoMessage(
                UUID.randomUUID(),
                cliente.getEmail().trim(),
                cliente.getNome(),
                assunto,
                template,
                variaveis
        );
    }
}
