package com.nowbox.nowbox_api.modules.cargo.repository;

import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
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
class ICargoRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    ICargoRepository cargoRepository;

    @Test
    @DisplayName("Return cargo with name filter")
    void findAllByFilterCase1() {
        List<CargoEntity> cargos = this.createScenario();

        Page<CargoEntity> result = cargoRepository.findAllByFilter(null, cargos.get(0).getNome(), null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(cargos.get(0).getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return cargo with idUnidade filter")
    void findAllByFilterCase2() {
        List<CargoEntity> cargos = this.createScenario();
        CargoEntity cargoOutraUnidade = cargos.get(2);

        Page<CargoEntity> result = cargoRepository.findAllByFilter(cargoOutraUnidade.getUnidade().getId(), null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(cargoOutraUnidade.getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of cargo")
    void findAllByFilterCase3() {
        this.createScenario();

        Page<CargoEntity> result = cargoRepository.findAllByFilter(null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of cargo")
    void findAllByFilterCase4() {
        Page<CargoEntity> result = cargoRepository.findAllByFilter(null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of cargo")
    void findAllByFilterCase5() {
        this.createScenario();

        Page<CargoEntity> result = cargoRepository.findAllByFilter(null, "Cargo Inexistente", null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Full filter match: Should return one element of cargo")
    void findAllByFilterCase6() {
        List<CargoEntity> cargos = this.createScenario();
        CargoEntity cargo1 = cargos.get(0);

        Page<CargoEntity> result = cargoRepository.findAllByFilter(cargo1.getUnidade().getId(), cargo1.getNome(), null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(cargo1.getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Filter mismatch between idUnidade and nome: Should not return elements of cargo")
    void findAllByFilterCase7() {
        List<CargoEntity> cargos = this.createScenario();
        CargoEntity cargo1 = cargos.get(0);
        CargoEntity cargoOutraUnidade = cargos.get(2);

        Page<CargoEntity> result = cargoRepository.findAllByFilter(cargoOutraUnidade.getUnidade().getId(), cargo1.getNome(), null);

        assertThat(result.getContent()).isEmpty();
    }

    private List<CargoEntity> createScenario() {
        // Cadastra um estado e uma unidade de teste
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("XX").build();
        this.em.persist(estado);

        UnidadeEntity unidade = UnidadeEntity.builder()
                .nome("Unidade").cnpj("11111111111111").endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(estado).build();
        this.em.persist(unidade);

        // Cadastra dois cargos
        CargoEntity cargo1 = CargoEntity.builder().nome("Cargo Test 1").unidade(unidade).build();
        CargoEntity cargo2 = CargoEntity.builder().nome("Cargo Test 2").unidade(unidade).build();

        this.em.persist(cargo1);
        this.em.persist(cargo2);

        // Cadastra uma segunda unidade com um cargo para testar o filtro por idUnidade
        UnidadeEntity outraUnidade = UnidadeEntity.builder()
                .nome("Outra Unidade").cnpj("22222222222222").endereco("Rua 2").numero("2")
                .bairro("Bairro 2").cep("22222222").cidade("Cidade 2").estado(estado).build();
        this.em.persist(outraUnidade);

        CargoEntity cargo3 = CargoEntity.builder().nome("Cargo Test 3").unidade(outraUnidade).build();
        this.em.persist(cargo3);

        return List.of(cargo1, cargo2, cargo3);
    }
}
