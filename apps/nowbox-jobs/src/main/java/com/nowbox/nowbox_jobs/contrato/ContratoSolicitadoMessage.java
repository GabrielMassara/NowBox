package com.nowbox.nowbox_jobs.contrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContratoSolicitadoMessage(
        UUID idSolicitacao,
        UUID idAluguel,
        UUID idBox,
        UUID idUnidade,
        String numeroBox,
        String cnpjLocadora,
        BigDecimal valor,
        LocalDate dataAssinatura,
        Contratante contratante
) {

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
}
