package com.nowbox.nowbox_api.modules.contrato.messaging;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

// Solicitacao de geracao de contrato enviada ao worker (nowbox-jobs). Carrega uma copia dos dados para que o worker nao precise consultar o banco
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

    public static ContratoSolicitadoMessage de(AluguelEntity aluguel) {
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

        return new ContratoSolicitadoMessage(
                UUID.randomUUID(),
                aluguel.getId(),
                box.getId(),
                box.getUnidade().getId(),
                box.getNumero(),
                box.getUnidade().getCnpj(),
                aluguel.getValor(),
                LocalDate.now(),
                contratante
        );
    }
}
