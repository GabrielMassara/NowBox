package com.nowbox.nowbox_jobs.contrato;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDateTime;
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
                CREATE TABLE tb_arquivo_aluguel (
                    id UUID PRIMARY KEY,
                    id_arquivo UUID NOT NULL UNIQUE REFERENCES tb_arquivo (id),
                    id_aluguel UUID NOT NULL,
                    id_box UUID NOT NULL,
                    salvo_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )""").update();
        repository = new ContratoRepository(jdbc);
    }

    private int contar(String tabela) {
        return jdbc.sql("SELECT COUNT(*) FROM " + tabela).query(Integer.class).single();
    }

    @Test
    @DisplayName("Should insert the arquivo and link it to the aluguel and the box")
    void registrarCase1() {
        ContratoSolicitadoMessage solicitacao = ContratoPdfGeneratorTest.solicitacao(true);

        boolean registrado = repository.registrar(solicitacao, "nowbox-contratos", "contratos/a.pdf", "contrato-teste.pdf",
                "application/pdf", 1234L, LocalDateTime.of(2026, 9, 28, 21, 30));

        assertThat(registrado).isTrue();
        assertThat(contar("tb_arquivo")).isEqualTo(1);
        assertThat(contar("tb_arquivo_aluguel")).isEqualTo(1);
        assertThat(jdbc.sql("SELECT tamanho FROM tb_arquivo WHERE chave = 'contratos/a.pdf'").query(Long.class).single()).isEqualTo(1234L);
        assertThat(jdbc.sql("SELECT id_aluguel FROM tb_arquivo_aluguel").query(UUID.class).single()).isEqualTo(solicitacao.idAluguel());
        assertThat(jdbc.sql("SELECT id_box FROM tb_arquivo_aluguel").query(UUID.class).single()).isEqualTo(solicitacao.idBox());
    }

    @Test
    @DisplayName("Should not duplicate the contrato when the same chave is registered again")
    void registrarCase2() {
        ContratoSolicitadoMessage solicitacao = ContratoPdfGeneratorTest.solicitacao(true);
        repository.registrar(solicitacao, "nowbox-contratos", "contratos/a.pdf", "contrato-teste.pdf", "application/pdf", 10L, LocalDateTime.now());

        boolean registrado = repository.registrar(solicitacao, "nowbox-contratos", "contratos/a.pdf", "contrato-teste.pdf", "application/pdf", 10L, LocalDateTime.now());

        assertThat(registrado).isFalse();
        assertThat(contar("tb_arquivo")).isEqualTo(1);
        assertThat(contar("tb_arquivo_aluguel")).isEqualTo(1);
    }
}
