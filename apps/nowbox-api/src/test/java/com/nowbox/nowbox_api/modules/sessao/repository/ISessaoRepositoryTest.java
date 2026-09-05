package com.nowbox.nowbox_api.modules.sessao.repository;

import com.nowbox.nowbox_api.modules.sessao.dto.SessaoCreateDTO;
import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ISessaoRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    ISessaoRepository sessaoRepository;

    @Test
    @DisplayName("Return sessao with name filter")
    void findAllByFiltroCase1() {
        this.criarCenario();

        Page<SessaoEntity> result = this.sessaoRepository.findAllByFiltro("Session Test", null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Session Test");
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return sessao with rota filter")
    void findAllByFiltroCase2() {
        this.criarCenario();

        Page<SessaoEntity> result = this.sessaoRepository.findAllByFiltro(null, "/test2", null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Session Test 2");
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of sessao")
    void findAllByFiltroCase3() {
        this.criarCenario();

        Page<SessaoEntity> result = this.sessaoRepository.findAllByFiltro(null, null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of sessao")
    void findAllByFiltroCase4() {
        Page<SessaoEntity> result = this.sessaoRepository.findAllByFiltro(null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of sessao")
    void findAllByFiltroCase5() {
        this.criarCenario();

        Page<SessaoEntity> result = this.sessaoRepository.findAllByFiltro("Session Test", "/test2", null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Full filter match: Should return one element of sessao")
    void findAllByFiltroCase6() {
        this.criarCenario();

        Page<SessaoEntity> result = this.sessaoRepository.findAllByFiltro("Session Test", "/test", null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Session Test");
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    private void criarCenario() {
        // First session
        SessaoCreateDTO sessao = SessaoCreateDTO.builder().nome("Session Test").rota("/test").build();
        this.criarSessao(sessao);

        // Second session
        sessao = SessaoCreateDTO.builder().nome("Session Test 2").rota("/test2").build();
        this.criarSessao(sessao);
    }

    // Funcao para cadastrar uma sessao para os testes
    private SessaoEntity criarSessao(SessaoCreateDTO sessao) {
        SessaoEntity novo = SessaoEntity.builder().nome(sessao.getNome()).rota(sessao.getRota()).build();
        this.em.persist(novo);
        return novo;
    }
}