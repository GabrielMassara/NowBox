package com.nowbox.nowbox_jobs.contrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ContratoSolicitadoMessage(
        UUID idSolicitacao,
        Tipo tipo,
        UUID idAluguel,
        UUID idBox,
        UUID idUnidade,
        String numeroBox,
        String cnpjLocadora,
        BigDecimal valor,
        LocalDate dataAssinatura,
        LocalDate dataContratoOriginal,
        List<Alteracao> alteracoes,
        Contratante contratante
) {

    public enum Tipo {
        CONTRATO,
        ADITIVO,
        DISTRATO
    }

    public record Alteracao(String campo, String valorAnterior, String valorNovo) {
    }

    public record Contratante(
            String nome,
            String profissao,
            String cpf,
            String rg,
            String endereco,
            String numeroEndereco,
            String complemento,
            String bairro,
            String cep,
            String cidade,
            String uf,
            String telefone,
            String email,
            boolean usarEnderecoParaCorrespondencia
    ) {
    }

    public boolean aditivo() {
        return tipo == Tipo.ADITIVO;
    }

    public boolean distrato() {
        return tipo == Tipo.DISTRATO;
    }

    // Uma alteracao por linha, usada no PDF e como descricao do aditivo no historico
    public String descricaoAlteracoes() {
        if (alteracoes == null) {
            return "";
        }
        return alteracoes.stream()
                .map(a -> a.campo() + ": " + a.valorAnterior() + " → " + a.valorNovo())
                .collect(Collectors.joining("\n"));
    }
}
