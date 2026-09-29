package com.nowbox.nowbox_jobs.contrato;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Gera o PDF do contrato a partir do modelo do JasperReports.
@Component
public class ContratoPdfGenerator {

    static final String MODELO_JASPER = "reports/contrato_teste.jasper";
    static final String MODELO_JRXML = "reports/contrato_teste.jrxml";

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA_POR_EXTENSO = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' uuuu", PT_BR);

    private volatile JasperReport modelo;

    public byte[] gerar(ContratoSolicitadoMessage solicitacao) {
        try {
            JasperPrint impressao = JasperFillManager.fillReport(carregarModelo(), montarParametros(solicitacao), new JREmptyDataSource());
            return JasperExportManager.exportReportToPdf(impressao);
        } catch (JRException e) {
            throw new IllegalStateException("Falha ao gerar o PDF do contrato do aluguel " + solicitacao.idAluguel(), e);
        }
    }

    Map<String, Object> montarParametros(ContratoSolicitadoMessage solicitacao) {
        ContratoSolicitadoMessage.Contratante c = solicitacao.contratante();

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("nome", c.nome());
        parametros.put("profissao", c.profissao());
        parametros.put("cpf", formatarCpf(c.cpf()));
        parametros.put("rg", c.rg());
        parametros.put("endereco", c.endereco());
        parametros.put("numeroEndereco", c.numeroEndereco());
        parametros.put("complemento", c.complemento() == null || c.complemento().isBlank() ? "-" : c.complemento());
        parametros.put("bairro", c.bairro());
        parametros.put("cep", formatarCep(c.cep()));
        parametros.put("cidade", c.cidade());
        parametros.put("uf", c.uf());
        parametros.put("telefone", c.telefone());
        parametros.put("email", c.email());
        parametros.put("endereco_correspondecia", montarEnderecoCorrespondencia(c));
        parametros.put("cnpj", formatarCnpj(solicitacao.cnpjLocadora()));
        parametros.put("valor", formatarValor(solicitacao.valor()));
        parametros.put("assinatura", formatarData(solicitacao.dataAssinatura()));
        parametros.put("numero", solicitacao.numeroBox());
        return parametros;
    }

    private JasperReport carregarModelo() {
        JasperReport carregado = modelo;
        if (carregado == null) {
            synchronized (this) {
                if (modelo == null) {
                    modelo = abrirModelo();
                }
                carregado = modelo;
            }
        }
        return carregado;
    }

    private JasperReport abrirModelo() {
        try (InputStream jasper = getClass().getClassLoader().getResourceAsStream(MODELO_JASPER)) {
            if (jasper != null) {
                return (JasperReport) JRLoader.loadObject(jasper);
            }
        } catch (IOException | JRException e) {
            throw new IllegalStateException("Falha ao carregar o modelo compilado " + MODELO_JASPER, e);
        }

        try (InputStream jrxml = getClass().getClassLoader().getResourceAsStream(MODELO_JRXML)) {
            if (jrxml == null) {
                throw new IllegalStateException("Modelo do contrato nao encontrado: " + MODELO_JASPER + " ou " + MODELO_JRXML);
            }
            return JasperCompileManager.compileReport(jrxml);
        } catch (IOException | JRException e) {
            throw new IllegalStateException("Falha ao compilar o modelo " + MODELO_JRXML, e);
        }
    }

    private String montarEnderecoCorrespondencia(ContratoSolicitadoMessage.Contratante c) {
        if (!c.usarEnderecoParaCorrespondencia()) {
            return "Não informado";
        }
        StringBuilder endereco = new StringBuilder(c.endereco()).append(", ").append(c.numeroEndereco());
        if (c.complemento() != null && !c.complemento().isBlank()) {
            endereco.append(" - ").append(c.complemento());
        }
        endereco.append(" - ").append(c.bairro()).append(", ").append(c.cidade()).append("/").append(c.uf());
        return endereco.toString();
    }

    static String formatarCpf(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    static String formatarCnpj(String cnpj) {
        if (cnpj == null || !cnpj.matches("\\d{14}")) {
            return cnpj;
        }
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
    }

    static String formatarCep(String cep) {
        if (cep == null || !cep.matches("\\d{8}")) {
            return cep;
        }
        return cep.substring(0, 5) + "-" + cep.substring(5);
    }

    static String formatarValor(BigDecimal valor) {
        return valor == null ? "" : NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    static String formatarData(LocalDate data) {
        return data == null ? "" : DATA_POR_EXTENSO.format(data);
    }
}
