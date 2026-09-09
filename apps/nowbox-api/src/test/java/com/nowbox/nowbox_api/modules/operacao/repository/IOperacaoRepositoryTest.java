package com.nowbox.nowbox_api.modules.operacao.repository;

import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class IOperacaoRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    IOperacaoRepository operacaoRepository;

    @Test
    @DisplayName("Return operacao with name filter")
    void findAllByFilterCase1() {
        List<OperacaoEntity> operacoes = this.createScenario();

        Page<OperacaoEntity> result = operacaoRepository.findAllByFilter(null, operacoes.get(0).getNome(), null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(operacoes.get(0).getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return operacao with codigo filter")
    void findAllByFilterCase2() {
        List<OperacaoEntity> operacoes = this.createScenario();

        Page<OperacaoEntity> result = operacaoRepository.findAllByFilter(null, null, operacoes.get(1).getCodigo(), null);

        assertThat(result.getContent().getFirst().getCodigo()).isEqualTo(operacoes.get(1).getCodigo());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return operacao with idModulo filter")
    void findAllByFilterCase3() {
        List<OperacaoEntity> operacoes = this.createScenario();
        OperacaoEntity operacaoOutroModulo = operacoes.get(2);

        Page<OperacaoEntity> result = operacaoRepository.findAllByFilter(operacaoOutroModulo.getModulo().getId(), null, null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(operacaoOutroModulo.getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of operacao")
    void findAllByFilterCase4() {
        this.createScenario();

        Page<OperacaoEntity> result = operacaoRepository.findAllByFilter(null, null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of operacao")
    void findAllByFilterCase5() {
        Page<OperacaoEntity> result = operacaoRepository.findAllByFilter(null, null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of operacao")
    void findAllByFilterCase6() {
        this.createScenario();

        Page<OperacaoEntity> result = operacaoRepository.findAllByFilter(null, "Operacao Inexistente", "NAOEXISTE", null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Full filter match: Should return one element of operacao")
    void findAllByFilterCase7() {
        List<OperacaoEntity> operacoes = this.createScenario();
        OperacaoEntity operacao1 = operacoes.get(0);

        Page<OperacaoEntity> result = operacaoRepository.findAllByFilter(
                operacao1.getModulo().getId(), operacao1.getNome(), operacao1.getCodigo(), null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(operacao1.getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Filter mismatch between idModulo and nome: Should not return elements of operacao")
    void findAllByFilterCase8() {
        List<OperacaoEntity> operacoes = this.createScenario();
        OperacaoEntity operacao1 = operacoes.get(0);
        OperacaoEntity operacaoOutroModulo = operacoes.get(2);

        Page<OperacaoEntity> result = operacaoRepository.findAllByFilter(
                operacaoOutroModulo.getModulo().getId(), operacao1.getNome(), null, null);

        assertThat(result.getContent()).isEmpty();
    }

    private List<OperacaoEntity> createScenario() {
        // Cadastra uma sessao e um modulo de teste
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        this.em.persist(sessao);

        ModuloEntity modulo = ModuloEntity.builder().nome("Modulo").rota("/modulo").sessao(sessao).build();
        this.em.persist(modulo);

        // Cadastra duas operacoes
        OperacaoEntity operacao1 = OperacaoEntity.builder().nome("Operacao Test 1").codigo("OP1").modulo(modulo).build();
        OperacaoEntity operacao2 = OperacaoEntity.builder().nome("Operacao Test 2").codigo("OP2").modulo(modulo).build();

        this.em.persist(operacao1);
        this.em.persist(operacao2);

        // Cadastra um segundo modulo com uma operacao para testar o filtro por idModulo
        ModuloEntity outroModulo = ModuloEntity.builder().nome("Outro Modulo").rota("/outroModulo").sessao(sessao).build();
        this.em.persist(outroModulo);

        OperacaoEntity operacao3 = OperacaoEntity.builder().nome("Operacao Test 3").codigo("OP3").modulo(outroModulo).build();
        this.em.persist(operacao3);

        return List.of(operacao1, operacao2, operacao3);
    }
}
