package com.nowbox.nowbox_api.modules.modulo.repository;

import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
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
class IModuloRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    IModuloRepository moduloRepository;

    @Test
    @DisplayName("Return modulo with name filter")
    void findAllByFilterCase1() {
        List<ModuloEntity> modulos = this.createScenario();

        Page<ModuloEntity> result = moduloRepository.findAllByFilter(null, modulos.get(0).getNome(), null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(modulos.get(0).getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return modulo with rota filter")
    void findAllByFilterCase2() {
        List<ModuloEntity> modulos = this.createScenario();

        Page<ModuloEntity> result = moduloRepository.findAllByFilter(null, null, modulos.get(1).getRota(), null);

        assertThat(result.getContent().getFirst().getRota()).isEqualTo(modulos.get(1).getRota());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return modulo with idSessao filter")
    void findAllByFilterCase3() {
        List<ModuloEntity> modulos = this.createScenario();
        ModuloEntity moduloOutraSessao = modulos.get(2);

        Page<ModuloEntity> result = moduloRepository.findAllByFilter(moduloOutraSessao.getSessao().getId(), null, null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(moduloOutraSessao.getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of modulo")
    void findAllByFilterCase4() {
        this.createScenario();

        Page<ModuloEntity> result = moduloRepository.findAllByFilter(null, null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of modulo")
    void findAllByFilterCase5() {
        Page<ModuloEntity> result = moduloRepository.findAllByFilter(null, null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of modulo")
    void findAllByFilterCase6() {
        this.createScenario();

        Page<ModuloEntity> result = moduloRepository.findAllByFilter(null, "Modulo Inexistente", "/naoExiste", null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Full filter match: Should return one element of modulo")
    void findAllByFilterCase7() {
        List<ModuloEntity> modulos = this.createScenario();
        ModuloEntity modulo1 = modulos.get(0);

        Page<ModuloEntity> result = moduloRepository.findAllByFilter(
                modulo1.getSessao().getId(), modulo1.getNome(), modulo1.getRota(), null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(modulo1.getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Filter mismatch between idSessao and nome: Should not return elements of modulo")
    void findAllByFilterCase8() {
        List<ModuloEntity> modulos = this.createScenario();
        ModuloEntity modulo1 = modulos.get(0);
        ModuloEntity moduloOutraSessao = modulos.get(2);

        Page<ModuloEntity> result = moduloRepository.findAllByFilter(
                moduloOutraSessao.getSessao().getId(), modulo1.getNome(), null, null);

        assertThat(result.getContent()).isEmpty();
    }

    private List<ModuloEntity> createScenario() {
        //Cadastra uma sessao de teste
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        this.em.persist(sessao);

        // Cadastra dois modulos
        ModuloEntity modulo1 = ModuloEntity.builder().nome("Modulo Test 1").rota("/modulo1").sessao(sessao).build();
        ModuloEntity modulo2 = ModuloEntity.builder().nome("Modulo Test 2").rota("/modulo2").sessao(sessao).build();

        this.em.persist(modulo1);
        this.em.persist(modulo2);

        // Cadastra uma segunda sessao com um modulo para testar o filtro por idSessao
        SessaoEntity outraSessao = SessaoEntity.builder().nome("Outra Sessao").rota("/outraRotaSessao").build();
        this.em.persist(outraSessao);

        ModuloEntity modulo3 = ModuloEntity.builder().nome("Modulo Test 3").rota("/modulo3").sessao(outraSessao).build();
        this.em.persist(modulo3);

        return List.of(modulo1, modulo2, modulo3);
    }
}