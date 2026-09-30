package com.nowbox.nowbox_jobs.email;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Troca os {{marcadores}} dos templates pelas variaveis da solicitacao
@Component
public class EmailTemplateRenderer {

    private static final Pattern MARCADOR = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");

    public String renderizarHtml(EmailTemplate template, Map<String, String> variaveis) {
        return renderizar(template, ".html", variaveis, true);
    }

    public String renderizarTexto(EmailTemplate template, Map<String, String> variaveis) {
        return renderizar(template, ".txt", variaveis, false);
    }

    private String renderizar(EmailTemplate template, String extensao, Map<String, String> variaveis, boolean escaparHtml) {
        Map<String, String> valores = new HashMap<>();
        valores.put("ano", String.valueOf(Year.now().getValue()));
        if (variaveis != null) {
            valores.putAll(variaveis);
        }

        Matcher matcher = MARCADOR.matcher(carregar(template, extensao));
        StringBuilder resultado = new StringBuilder();
        while (matcher.find()) {
            String valor = valores.getOrDefault(matcher.group(1), "");
            matcher.appendReplacement(resultado, Matcher.quoteReplacement(escaparHtml ? HtmlUtils.htmlEscape(valor) : valor));
        }
        matcher.appendTail(resultado);
        return resultado.toString();
    }

    private String carregar(EmailTemplate template, String extensao) {
        try {
            return new ClassPathResource("email/templates/" + template.getArquivo() + extensao).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Nao foi possivel ler o template de email " + template.getArquivo() + extensao, e);
        }
    }
}
