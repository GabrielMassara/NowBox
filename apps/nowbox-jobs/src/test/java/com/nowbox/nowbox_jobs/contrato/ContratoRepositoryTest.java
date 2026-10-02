package com.nowbox.nowbox_jobs.contrato;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContratoRepositoryTest {

    private JdbcClient jdbc;
    private ContratoRepository repository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = JdbcClient.create(dataSource);
        jdbc.sql("""
                CREATE TABLE tb_arquivo (
                    id UUID PRIMARY KEY,
                    bucket VARCHAR(100) NOT NULL,
                    chave VARCHAR(500) NOT NULL UNIQUE,
                    nome_original VARCHAR(200) NOT NULL,
                    content_type VARCHAR(100) NOT NULL,
                    tamanho BIGINT NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )""").update();
        jdbc.sql("""
                CREATE TABLE tb_aluguel (
                    id UUID PRIMARY KEY,
                    status VARCHAR(40) NOT NULL,
                    id_arquivo_contrato UUID UNIQUE REFERENCES tb_arquivo (id),
                    id_arquivo_distrato UUID UNIQUE REFERENCES tb_arquivo (id)
                )""").update();
        jdbc.sql("""
                CREATE TABLE tb_arquivo_aluguel (
                    id UUID PRIMARY KEY,
                    id_arquivo UUID NOT NULL UNIQUE REFERENCES tb_arquivo (id),
                    id_aluguel UUID NOT NULL,
                    id_box UUID NOT NULL,
                    descricao TEXT,
                    salvo_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    pendente_assinatura BOOLEAN NOT NULL DEFAULT FALSE
                )""").update();
        repository = new ContratoRepository(jdbc);
    }

    private int contar(String tabela) {
        return jdbc.sql("SELECT COUNT(*) FROM " + tabela).query(Integer.class).single();
    }

    private void criarAluguel(UUID id) {
        jdbc.sql("INSERT INTO tb_aluguel (id, status) VALUES (:id, 'PENDENTE_ASSINATURA_CONTRATO')").param("id", id).update();
    }

    private void definirStatus(UUID id, String status) {
        jdbc.sql("UPDATE tb_aluguel SET status = :status WHERE id = :id").param("status", status).param("id", id).update();
    }

    private String chaveDoContratoOriginal() {
        return jdbc.sql("SELECT a.chave FROM tb_aluguel l JOIN tb_arquivo a ON a.id = l.id_arquivo_contrato").query(String.class).single();
    }

    @Test
    @DisplayName("Should reference the original contrato directly in the aluguel without creating an aditivo")
    void registrarCase1() {
        ContratoSolicitadoMessage solicitacao = ContratoPdfGeneratorTest.solicitacao(true);
        criarAluguel(solicitacao.idAluguel());

        boolean registrado = repository.registrar(solicitacao, "nowbox-contratos", "contratos/a.pdf", "contrato-teste.pdf",
                "application/pdf", 1234L, LocalDateTime.of(2026, 9, 28, 21, 30));

        assertThat(registrado).isTrue();
        assertThat(contar("tb_arquivo")).isEqualTo(1);
        assertThat(contar("tb_arquivo_aluguel")).isZero();
        assertThat(jdbc.sql("SELECT tamanho FROM tb_arquivo WHERE chave = 'contratos/a.pdf'").query(Long.class).single()).isEqualTo(1234L);
        assertThat(chaveDoContratoOriginal()).isEqualTo("contratos/a.pdf");
    }

    @Test
    @DisplayName("Should register the aditivo linked to the aluguel and the box and keep the original contrato")
    void registrarCase2() {
        ContratoSolicitadoMessage contrato = ContratoPdfGeneratorTest.solicitacao(true);
        criarAluguel(contrato.idAluguel());
        repository.registrar(contrato, "nowbox-contratos", "contratos/a.pdf", "contrato-teste.pdf", "application/pdf", 10L, LocalDateTime.now());

        ContratoSolicitadoMessage aditivo = new ContratoSolicitadoMessage(UUID.randomUUID(), ContratoSolicitadoMessage.Tipo.ADITIVO, contrato.idAluguel(),
                contrato.idBox(), contrato.idUnidade(), "101", contrato.cnpjLocadora(), contrato.valor(), contrato.dataAssinatura(), contrato.dataAssinatura(),
                List.of(new ContratoSolicitadoMessage.Alteracao("Valor", "R$ 100,00", "R$ 120,00")), contrato.contratante());

        definirStatus(contrato.idAluguel(), "PENDENTE_ASSINATURA_ADITIVO");
        boolean registrado = repository.registrar(aditivo, "nowbox-contratos", "contratos/aditivos/b.pdf", "aditivo.pdf", "application/pdf", 20L,
                LocalDateTime.of(2026, 10, 5, 10, 0));

        assertThat(registrado).isTrue();
        assertThat(contar("tb_arquivo")).isEqualTo(2);
        assertThat(contar("tb_arquivo_aluguel")).isEqualTo(1);
        assertThat(jdbc.sql("SELECT id_aluguel FROM tb_arquivo_aluguel").query(UUID.class).single()).isEqualTo(aditivo.idAluguel());
        assertThat(jdbc.sql("SELECT id_box FROM tb_arquivo_aluguel").query(UUID.class).single()).isEqualTo(aditivo.idBox());
        assertThat(jdbc.sql("SELECT descricao FROM tb_arquivo_aluguel").query(String.class).single()).isEqualTo("Valor: R$ 100,00 → R$ 120,00");
        assertThat(chaveDoContratoOriginal()).isEqualTo("contratos/a.pdf");
    }

    @Test
    @DisplayName("Should not duplicate the documento when the same chave is registered again")
    void registrarCase3() {
        ContratoSolicitadoMessage solicitacao = ContratoPdfGeneratorTest.solicitacao(true);
        criarAluguel(solicitacao.idAluguel());
        repository.registrar(solicitacao, "nowbox-contratos", "contratos/a.pdf", "contrato-teste.pdf", "application/pdf", 10L, LocalDateTime.now());

        boolean registrado = repository.registrar(solicitacao, "nowbox-contratos", "contratos/a.pdf", "contrato-teste.pdf", "application/pdf", 10L, LocalDateTime.now());

        assertThat(registrado).isFalse();
        assertThat(contar("tb_arquivo")).isEqualTo(1);
    }

    @Test
    @DisplayName("Should reference the distrato directly in the aluguel without creating an aditivo and keep the original contrato")
    void registrarDistrato() {
        ContratoSolicitadoMessage contrato = ContratoPdfGeneratorTest.solicitacao(true);
        criarAluguel(contrato.idAluguel());
        repository.registrar(contrato, "nowbox-contratos", "contratos/a.pdf", "contrato-teste.pdf", "application/pdf", 10L, LocalDateTime.now());

        ContratoSolicitadoMessage distrato = new ContratoSolicitadoMessage(UUID.randomUUID(), ContratoSolicitadoMessage.Tipo.DISTRATO, contrato.idAluguel(),
                contrato.idBox(), contrato.idUnidade(), "101", contrato.cnpjLocadora(), contrato.valor(), contrato.dataAssinatura(), contrato.dataAssinatura(),
                List.of(), contrato.contratante());
        definirStatus(contrato.idAluguel(), "PENDENTE_ASSINATURA_DISTRATO");
        boolean registrado = repository.registrar(distrato, "nowbox-contratos", "contratos/distratos/c.pdf", "distrato.pdf", "application/pdf", 30L, LocalDateTime.now());

        assertThat(registrado).isTrue();
        assertThat(contar("tb_arquivo")).isEqualTo(2);
        assertThat(contar("tb_arquivo_aluguel")).isZero();
        assertThat(chaveDoContratoOriginal()).isEqualTo("contratos/a.pdf");
        assertThat(jdbc.sql("SELECT a.chave FROM tb_aluguel l JOIN tb_arquivo a ON a.id = l.id_arquivo_distrato").query(String.class).single()).isEqualTo("contratos/distratos/c.pdf");
    }

    @Test
    @DisplayName("Should not register the documento when the aluguel no longer awaits it because the signature was cancelled")
    void registrarAssinaturaCancelada() {
        ContratoSolicitadoMessage contrato = ContratoPdfGeneratorTest.solicitacao(true);
        criarAluguel(contrato.idAluguel());
        definirStatus(contrato.idAluguel(), "INATIVO");

        assertThat(repository.aguardandoGeracao(contrato)).isFalse();
        boolean registrado = repository.registrar(contrato, "nowbox-contratos", "contratos/a.pdf", "contrato-teste.pdf", "application/pdf", 10L, LocalDateTime.now());

        assertThat(registrado).isFalse();
        assertThat(contar("tb_arquivo")).isZero();
        assertThat(jdbc.sql("SELECT id_arquivo_contrato FROM tb_aluguel").query(UUID.class).optional()).isEmpty();
    }

    @Test
    @DisplayName("Should not register a late aditivo after the aditivo signature was cancelled")
    void registrarAditivoCancelado() {
        ContratoSolicitadoMessage contrato = ContratoPdfGeneratorTest.solicitacao(true);
        criarAluguel(contrato.idAluguel());
        definirStatus(contrato.idAluguel(), "ATIVO");
        ContratoSolicitadoMessage aditivo = new ContratoSolicitadoMessage(UUID.randomUUID(), ContratoSolicitadoMessage.Tipo.ADITIVO, contrato.idAluguel(),
                contrato.idBox(), contrato.idUnidade(), "101", contrato.cnpjLocadora(), contrato.valor(), contrato.dataAssinatura(), contrato.dataAssinatura(),
                List.of(), contrato.contratante());

        assertThat(repository.aguardandoGeracao(aditivo)).isFalse();
        assertThat(repository.registrar(aditivo, "nowbox-contratos", "contratos/aditivos/b.pdf", "aditivo.pdf", "application/pdf", 20L, LocalDateTime.now())).isFalse();
        assertThat(contar("tb_arquivo_aluguel")).isZero();
    }

    @Test
    @DisplayName("Should not await any documento for an aluguel that does not exist")
    void aguardandoGeracaoAluguelInexistente() {
        assertThat(repository.aguardandoGeracao(ContratoPdfGeneratorTest.solicitacao(true))).isFalse();
    }
}
