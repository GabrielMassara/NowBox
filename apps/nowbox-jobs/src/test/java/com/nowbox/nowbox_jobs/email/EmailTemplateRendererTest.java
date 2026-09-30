package com.nowbox.nowbox_jobs.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateRendererTest {

    private final EmailTemplateRenderer renderer = new EmailTemplateRenderer();

    private static final Map<String, String> VARIAVEIS = Map.of(
            "nome", "Maria", "numeroBox", "101", "unidade", "Unidade Centro", "valor", "R$ 1.234,50", "data", "28/09/2026");

    @Test
    @DisplayName("Should fill every marker of the html template and reference the embedded logo")
    void renderizarHtml() {
        String html = renderer.renderizarHtml(EmailTemplate.ALUGUEL_REGISTRADO, VARIAVEIS);

        assertThat(html).contains("Olá, Maria!", "101", "Unidade Centro", "R$ 1.234,50", "28/09/2026", String.valueOf(Year.now().getValue()));
        assertThat(html).contains("cid:" + EmailSender.CID_LOGO);
        assertThat(html).doesNotContain("{{");
    }

    @Test
    @DisplayName("Should escape html in the values so customer data cannot inject markup")
    void escaparHtml() {
        String html = renderer.renderizarHtml(EmailTemplate.ALUGUEL_REGISTRADO,
                Map.of("nome", "<script>alert(1)</script> & Cia", "numeroBox", "1"));

        assertThat(html).doesNotContain("<script>");
        assertThat(html).contains("&lt;script&gt;alert(1)&lt;/script&gt; &amp; Cia");
    }

    @Test
    @DisplayName("Should render the plain text alternative without html escaping")
    void renderizarTexto() {
        String texto = renderer.renderizarTexto(EmailTemplate.ALUGUEL_REGISTRADO, Map.of("nome", "Maria & Cia", "numeroBox", "101"));

        assertThat(texto).contains("Olá, Maria & Cia!", "Box: 101");
        assertThat(texto).doesNotContain("{{");
    }

    @Test
    @DisplayName("Should replace markers without a value by an empty text")
    void marcadorSemValor() {
        String texto = renderer.renderizarTexto(EmailTemplate.ALUGUEL_REGISTRADO, null);

        assertThat(texto).contains("Olá, !").doesNotContain("{{");
    }
}
