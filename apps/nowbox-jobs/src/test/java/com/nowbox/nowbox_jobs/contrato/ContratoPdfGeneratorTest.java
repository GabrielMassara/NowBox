package com.nowbox.nowbox_jobs.contrato;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContratoPdfGeneratorTest {

    private final ContratoPdfGenerator generator = new ContratoPdfGenerator();

    static ContratoSolicitadoMessage solicitacao(boolean usarEnderecoParaCorrespondencia) {
        ContratoSolicitadoMessage.Contratante contratante = new ContratoSolicitadoMessage.Contratante(
                "Maria da Silva", "Engenheira", "12345678901", "MG1234567", "Rua das Flores", "123", null,
                "Centro", "30123456", "Belo Horizonte", "MG", "31999998888", "maria@email.com", usarEnderecoParaCorrespondencia);

        return new ContratoSolicitadoMessage(UUID.randomUUID(), ContratoSolicitadoMessage.Tipo.CONTRATO, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "101", "12345678000199", new BigDecimal("1234.50"), LocalDate.of(2026, 9, 28), LocalDate.of(2026, 9, 28), List.of(), contratante);
    }

    @Test
    @DisplayName("Should generate a PDF from the contrato template")
    void gerarCase1() {
        byte[] pdf = generator.gerar(solicitacao(true));

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("Should compile the jrxml source and render it in a single page with the logo")
    void compilarJrxmlCase1() throws Exception {
        try (java.io.InputStream jrxml = getClass().getClassLoader().getResourceAsStream(ContratoPdfGenerator.MODELO_JRXML)) {
            JasperPrint impressao = JasperFillManager.fillReport(JasperCompileManager.compileReport(jrxml),
                    generator.montarParametros(solicitacao(true)), new JREmptyDataSource());
            byte[] pdf = JasperExportManager.exportReportToPdf(impressao);

            assertThat(impressao.getPages()).hasSize(1);
            assertThat(new String(pdf, StandardCharsets.ISO_8859_1)).contains("/Subtype /Image");
        }
    }

    @Test
    @DisplayName("Should send every template field, formatted, as a report parameter")
    void montarParametrosCase1() {
        Map<String, Object> parametros = generator.montarParametros(solicitacao(true));

        assertThat(parametros).containsEntry("nome", "Maria da Silva")
                .containsEntry("profissao", "Engenheira")
                .containsEntry("cpf", "123.456.789-01")
                .containsEntry("rg", "MG1234567")
                .containsEntry("endereco", "Rua das Flores")
                .containsEntry("numeroEndereco", "123")
                .containsEntry("complemento", "-")
                .containsEntry("bairro", "Centro")
                .containsEntry("cep", "30123-456")
                .containsEntry("cidade", "Belo Horizonte")
                .containsEntry("uf", "MG")
                .containsEntry("telefone", "31999998888")
                .containsEntry("email", "maria@email.com")
                .containsEntry("cnpj", "12.345.678/0001-99")
                .containsEntry("assinatura", "28 de setembro de 2026")
                .containsEntry("numero", "101");
        assertThat((String) parametros.get("valor")).contains("1.234,50");
        assertThat((String) parametros.get("endereco_correspondecia")).contains("Rua das Flores, 123").contains("Belo Horizonte/MG");
    }

    static ContratoSolicitadoMessage aditivo() {
        ContratoSolicitadoMessage base = solicitacao(true);
        return new ContratoSolicitadoMessage(base.idSolicitacao(), ContratoSolicitadoMessage.Tipo.ADITIVO, base.idAluguel(), base.idBox(),
                base.idUnidade(), base.numeroBox(), base.cnpjLocadora(), base.valor(), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 9, 28),
                List.of(new ContratoSolicitadoMessage.Alteracao("Valor", "R$ 100,00", "R$ 120,00"),
                        new ContratoSolicitadoMessage.Alteracao("Observação", "-", "Box com chave")),
                base.contratante());
    }

    @Test
    @DisplayName("Should send the original contrato date and the changes, one per line, as report parameters of the aditivo")
    void montarParametrosAditivo() {
        Map<String, Object> parametros = generator.montarParametros(aditivo());

        assertThat(parametros).containsEntry("contratoOriginal", "28 de setembro de 2026")
                .containsEntry("assinatura", "5 de outubro de 2026")
                .containsEntry("alteracoes", "Valor: R$ 100,00 → R$ 120,00\nObservação: - → Box com chave");
    }

    @Test
    @DisplayName("Should say the correspondence address was not informed when the cliente does not use it")
    void montarParametrosCase2() {
        Map<String, Object> parametros = generator.montarParametros(solicitacao(false));

        assertThat(parametros).containsEntry("endereco_correspondecia", "Não informado");
    }

    static ContratoSolicitadoMessage distrato() {
        ContratoSolicitadoMessage base = solicitacao(true);
        return new ContratoSolicitadoMessage(base.idSolicitacao(), ContratoSolicitadoMessage.Tipo.DISTRATO, base.idAluguel(), base.idBox(),
                base.idUnidade(), base.numeroBox(), base.cnpjLocadora(), base.valor(), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 9, 28),
                List.of(), base.contratante());
    }

    @Test
    @DisplayName("Should send the original contrato date and the termination date as report parameters of the distrato")
    void montarParametrosDistrato() {
        Map<String, Object> parametros = generator.montarParametros(distrato());

        assertThat(parametros).containsEntry("contratoOriginal", "28 de setembro de 2026")
                .containsEntry("assinatura", "5 de outubro de 2026")
                .containsEntry("numero", "101");
    }
}
