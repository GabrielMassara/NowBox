package com.nowbox.nowbox_jobs.contrato;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

// Registra a referencia do documento gravado no MinIO. O contrato original e o distrato sao referenciados direto no aluguel
// e os aditivos entram em tb_arquivo_aluguel
@Repository
@RequiredArgsConstructor
public class ContratoRepository {

    private final JdbcClient jdbc;

    // Indica se o aluguel ainda aguarda este documento. E falso quando a assinatura foi cancelada ou o aluguel nao existe mais
    public boolean aguardandoGeracao(ContratoSolicitadoMessage solicitacao) {
        return solicitacao.statusEsperado().equals(statusDoAluguel(solicitacao.idAluguel(), false));
    }

    private String statusDoAluguel(UUID idAluguel, boolean travar) {
        return jdbc.sql("SELECT status FROM tb_aluguel WHERE id = :id" + (travar ? " FOR UPDATE" : ""))
                .param("id", idAluguel)
                .query(String.class)
                .optional()
                .orElse(null);
    }

    // Retorna false quando a chave ja estava registrada sem duplicar o documento ou quando o aluguel deixou de aguardar o documento
    @Transactional
    public boolean registrar(ContratoSolicitadoMessage solicitacao, String bucket, String chave, String nomeArquivo,
                             String contentType, long tamanho, LocalDateTime salvoEm) {
        // Trava o aluguel para que um cancelamento concorrente espere este registro terminar, em vez de ser ignorado por ele
        if (!solicitacao.statusEsperado().equals(statusDoAluguel(solicitacao.idAluguel(), true))) {
            return false;
        }

        UUID idArquivo = UUID.randomUUID();

        int inseridos = jdbc.sql("""
                        INSERT INTO tb_arquivo (id, bucket, chave, nome_original, content_type, tamanho)
                        SELECT :id, :bucket, :chave, :nome, :contentType, :tamanho
                        WHERE NOT EXISTS (SELECT 1 FROM tb_arquivo WHERE chave = :chave)
                        """)
                .param("id", idArquivo)
                .param("bucket", bucket)
                .param("chave", chave)
                .param("nome", nomeArquivo)
                .param("contentType", contentType)
                .param("tamanho", tamanho)
                .update();

        if (inseridos == 0) {
            return false;
        }

        if (solicitacao.aditivo()) {
            registrarAditivo(solicitacao, idArquivo, salvoEm);
        } else if (solicitacao.distrato()) {
            registrarDistrato(solicitacao, idArquivo);
        } else {
            registrarContrato(solicitacao, idArquivo);
        }

        return true;
    }

    private void registrarContrato(ContratoSolicitadoMessage solicitacao, UUID idArquivo) {
        jdbc.sql("UPDATE tb_aluguel SET id_arquivo_contrato = :idArquivo WHERE id = :idAluguel")
                .param("idArquivo", idArquivo)
                .param("idAluguel", solicitacao.idAluguel())
                .update();
    }

    private void registrarDistrato(ContratoSolicitadoMessage solicitacao, UUID idArquivo) {
        jdbc.sql("UPDATE tb_aluguel SET id_arquivo_distrato = :idArquivo WHERE id = :idAluguel")
                .param("idArquivo", idArquivo)
                .param("idAluguel", solicitacao.idAluguel())
                .update();
    }

    private void registrarAditivo(ContratoSolicitadoMessage solicitacao, UUID idArquivo, LocalDateTime salvoEm) {
        jdbc.sql("""
                        INSERT INTO tb_arquivo_aluguel (id, id_arquivo, id_aluguel, id_box, descricao, salvo_em, pendente_assinatura)
                        VALUES (:id, :idArquivo, :idAluguel, :idBox, :descricao, :salvoEm, TRUE)
                        """)
                .param("id", UUID.randomUUID())
                .param("idArquivo", idArquivo)
                .param("idAluguel", solicitacao.idAluguel())
                .param("idBox", solicitacao.idBox())
                .param("descricao", solicitacao.descricaoAlteracoes())
                .param("salvoEm", salvoEm)
                .update();
    }
}
