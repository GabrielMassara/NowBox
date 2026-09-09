package com.nowbox.nowbox_api.modules.unidade.repository;

import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class IUnidadeRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    IUnidadeRepository unidadeRepository;

    @Test
    @DisplayName("Return unidade with name filter")
    void findAllByFilterCase1() {
        List<UnidadeEntity> unidades = this.createScenario();

        Page<UnidadeEntity> result = unidadeRepository.findAllByFilter(null, unidades.get(0).getNome(), null, null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(unidades.get(0).getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return unidade with cnpj filter")
    void findAllByFilterCase2() {
        List<UnidadeEntity> unidades = this.createScenario();

        Page<UnidadeEntity> result = unidadeRepository.findAllByFilter(null, null, unidades.get(1).getCnpj(), null, null);

        assertThat(result.getContent().getFirst().getCnpj()).isEqualTo(unidades.get(1).getCnpj());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return unidade with idEstado filter")
    void findAllByFilterCase3() {
        List<UnidadeEntity> unidades = this.createScenario();
        UnidadeEntity unidadeOutroEstado = unidades.get(2);

        Page<UnidadeEntity> result = unidadeRepository.findAllByFilter(unidadeOutroEstado.getEstado().getId(), null, null, null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(unidadeOutroEstado.getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of unidade")
    void findAllByFilterCase4() {
        this.createScenario();

        Page<UnidadeEntity> result = unidadeRepository.findAllByFilter(null, null, null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of unidade")
    void findAllByFilterCase5() {
        Page<UnidadeEntity> result = unidadeRepository.findAllByFilter(null, null, null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of unidade")
    void findAllByFilterCase6() {
        this.createScenario();

        Page<UnidadeEntity> result = unidadeRepository.findAllByFilter(null, "Unidade Inexistente", null, "Cidade Inexistente", null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should not return soft deleted unidade")
    void findAllByFilterCase7() {
        List<UnidadeEntity> unidades = this.createScenario();
        UnidadeEntity unidade1 = unidades.get(0);
        unidade1.setDeletedAt(LocalDateTime.now());
        this.em.persist(unidade1);

        Page<UnidadeEntity> result = unidadeRepository.findAllByFilter(null, unidade1.getNome(), null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should return unidade when id exists and is not deleted")
    void findByIdAndDeletedAtIsNullCase1() {
        List<UnidadeEntity> unidades = this.createScenario();
        UnidadeEntity unidade1 = unidades.get(0);

        Optional<UnidadeEntity> result = unidadeRepository.findByIdAndDeletedAtIsNull(unidade1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getNome()).isEqualTo(unidade1.getNome());
    }

    @Test
    @DisplayName("Should not return unidade when id is soft deleted")
    void findByIdAndDeletedAtIsNullCase2() {
        List<UnidadeEntity> unidades = this.createScenario();
        UnidadeEntity unidade1 = unidades.get(0);
        unidade1.setDeletedAt(LocalDateTime.now());
        this.em.persist(unidade1);

        Optional<UnidadeEntity> result = unidadeRepository.findByIdAndDeletedAtIsNull(unidade1.getId());

        assertThat(result).isEmpty();
    }

    private List<UnidadeEntity> createScenario() {
        // Cadastra um estado de teste
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("XX").build();
        this.em.persist(estado);

        // Cadastra duas unidades
        UnidadeEntity unidade1 = UnidadeEntity.builder()
                .nome("Unidade Test 1").cnpj("11111111111111").endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(estado).build();
        UnidadeEntity unidade2 = UnidadeEntity.builder()
                .nome("Unidade Test 2").cnpj("22222222222222").endereco("Rua 2").numero("2")
                .bairro("Bairro 2").cep("22222222").cidade("Cidade 2").estado(estado).build();

        this.em.persist(unidade1);
        this.em.persist(unidade2);

        // Cadastra um segundo estado com uma unidade para testar o filtro por idEstado
        EstadoEntity outroEstado = EstadoEntity.builder().nome("Outro Estado").uf("YY").build();
        this.em.persist(outroEstado);

        UnidadeEntity unidade3 = UnidadeEntity.builder()
                .nome("Unidade Test 3").cnpj("33333333333333").endereco("Rua 3").numero("3")
                .bairro("Bairro 3").cep("33333333").cidade("Cidade 3").estado(outroEstado).build();
        this.em.persist(unidade3);

        return List.of(unidade1, unidade2, unidade3);
    }
}
