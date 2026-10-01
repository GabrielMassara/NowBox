package com.nowbox.nowbox_api.modules.contrato.messaging;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Solicitacao de geracao de documento enviada ao worker (nowbox-jobs). Carrega uma copia dos dados para que o worker nao precise consultar o banco.
// O tipo CONTRATO gera o contrato original do aluguel e o ADITIVO gera um documento com as alteracoes feitas nele
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
        ADITIVO
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

    public static ContratoSolicitadoMessage contrato(AluguelEntity aluguel) {
        return de(aluguel, Tipo.CONTRATO, List.of());
    }

    public static ContratoSolicitadoMessage aditivo(AluguelEntity aluguel, List<Alteracao> alteracoes) {
        return de(aluguel, Tipo.ADITIVO, alteracoes);
    }

    private static ContratoSolicitadoMessage de(AluguelEntity aluguel, Tipo tipo, List<Alteracao> alteracoes) {
        BoxEntity box = aluguel.getBox();
        ClienteEntity cliente = aluguel.getCliente();

        Contratante contratante = new Contratante(
                cliente.getNome(),
                cliente.getProfissao(),
                cliente.getCpf(),
                cliente.getRg(),
                cliente.getEndereco(),
                cliente.getNumero(),
                cliente.getComplemento(),
                cliente.getBairro(),
                cliente.getCep(),
                cliente.getCidade(),
                cliente.getEstado() != null ? cliente.getEstado().getUf() : null,
                cliente.getTelefone(),
                cliente.getEmail(),
                Boolean.TRUE.equals(cliente.getEnderecoCorrespondencia())
        );

        LocalDate hoje = LocalDate.now();

        return new ContratoSolicitadoMessage(
                UUID.randomUUID(),
                tipo,
                aluguel.getId(),
                box.getId(),
                box.getUnidade().getId(),
                box.getNumero(),
                box.getUnidade().getCnpj(),
                aluguel.getValor(),
                hoje,
                aluguel.getCreatedAt() != null ? aluguel.getCreatedAt().toLocalDate() : hoje,
                alteracoes,
                contratante
        );
    }
}
