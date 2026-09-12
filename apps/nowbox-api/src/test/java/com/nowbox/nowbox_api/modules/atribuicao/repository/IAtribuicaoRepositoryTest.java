package com.nowbox.nowbox_api.modules.atribuicao.repository;

import com.nowbox.nowbox_api.modules.atribuicao.entity.AtribuicaoEntity;
import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import com.nowbox.nowbox_api.modules.usuario.entity.UsuarioEntity;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class IAtribuicaoRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    IAtribuicaoRepository atribuicaoRepository;

    @Test
    @DisplayName("Return atribuicao with idUsuario filter")
    void findAllByFilterCase1() {
        List<AtribuicaoEntity> atribuicoes = this.createScenario();

        Page<AtribuicaoEntity> result = atribuicaoRepository.findAllByFilter(atribuicoes.get(0).getUsuario().getId(), null, null);

        assertThat(result.getContent().getFirst().getId()).isEqualTo(atribuicoes.get(0).getId());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return atribuicao with idCargo filter")
    void findAllByFilterCase2() {
        List<AtribuicaoEntity> atribuicoes = this.createScenario();
        AtribuicaoEntity atribuicaoOutroCargo = atribuicoes.get(2);

        Page<AtribuicaoEntity> result = atribuicaoRepository.findAllByFilter(null, atribuicaoOutroCargo.getCargo().getId(), null);

        assertThat(result.getContent().getFirst().getId()).isEqualTo(atribuicaoOutroCargo.getId());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of atribuicao")
    void findAllByFilterCase3() {
        this.createScenario();

        Page<AtribuicaoEntity> result = atribuicaoRepository.findAllByFilter(null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of atribuicao")
    void findAllByFilterCase4() {
        Page<AtribuicaoEntity> result = atribuicaoRepository.findAllByFilter(null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of atribuicao")
    void findAllByFilterCase5() {
        this.createScenario();

        Page<AtribuicaoEntity> result = atribuicaoRepository.findAllByFilter(UUID.randomUUID(), UUID.randomUUID(), null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Full filter match: Should return one element of atribuicao")
    void findAllByFilterCase6() {
        List<AtribuicaoEntity> atribuicoes = this.createScenario();
        AtribuicaoEntity atribuicao1 = atribuicoes.get(0);

        Page<AtribuicaoEntity> result = atribuicaoRepository.findAllByFilter(
                atribuicao1.getUsuario().getId(), atribuicao1.getCargo().getId(), null);

        assertThat(result.getContent().getFirst().getId()).isEqualTo(atribuicao1.getId());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Filter mismatch between idUsuario and idCargo: Should not return elements of atribuicao")
    void findAllByFilterCase7() {
        List<AtribuicaoEntity> atribuicoes = this.createScenario();
        AtribuicaoEntity atribuicao1 = atribuicoes.get(0);
        AtribuicaoEntity atribuicaoOutroCargo = atribuicoes.get(2);

        Page<AtribuicaoEntity> result = atribuicaoRepository.findAllByFilter(
                atribuicao1.getUsuario().getId(), atribuicaoOutroCargo.getCargo().getId(), null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should not return soft deleted atribuicao")
    void findAllByFilterCase8() {
        List<AtribuicaoEntity> atribuicoes = this.createScenario();
        AtribuicaoEntity atribuicao1 = atribuicoes.get(0);
        atribuicao1.setDeletedAt(LocalDateTime.now());
        this.em.persist(atribuicao1);

        Page<AtribuicaoEntity> result = atribuicaoRepository.findAllByFilter(atribuicao1.getUsuario().getId(), null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should return atribuicao when id exists and is not deleted")
    void findByIdAndDeletedAtIsNullCase1() {
        List<AtribuicaoEntity> atribuicoes = this.createScenario();
        AtribuicaoEntity atribuicao1 = atribuicoes.get(0);

        Optional<AtribuicaoEntity> result = atribuicaoRepository.findByIdAndDeletedAtIsNull(atribuicao1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(atribuicao1.getId());
    }

    @Test
    @DisplayName("Should not return atribuicao when id is soft deleted")
    void findByIdAndDeletedAtIsNullCase2() {
        List<AtribuicaoEntity> atribuicoes = this.createScenario();
        AtribuicaoEntity atribuicao1 = atribuicoes.get(0);
        atribuicao1.setDeletedAt(LocalDateTime.now());
        this.em.persist(atribuicao1);

        Optional<AtribuicaoEntity> result = atribuicaoRepository.findByIdAndDeletedAtIsNull(atribuicao1.getId());

        assertThat(result).isEmpty();
    }

    private List<AtribuicaoEntity> createScenario() {
        // Cadastra um estado e uma unidade de teste
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("XX").build();
        this.em.persist(estado);

        UnidadeEntity unidade = UnidadeEntity.builder()
                .nome("Unidade").cnpj("11111111111111").endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(estado).build();
        this.em.persist(unidade);

        // Cadastra tres cargos de teste
        CargoEntity cargo1 = CargoEntity.builder().nome("Cargo Test 1").unidade(unidade).build();
        CargoEntity cargo2 = CargoEntity.builder().nome("Cargo Test 2").unidade(unidade).build();
        CargoEntity cargo3 = CargoEntity.builder().nome("Cargo Test 3").unidade(unidade).build();
        this.em.persist(cargo1);
        this.em.persist(cargo2);
        this.em.persist(cargo3);

        // Cadastra dois usuarios de teste
        UsuarioEntity usuario1 = UsuarioEntity.builder()
                .nome("Usuario Test 1").email("usuario1@test.com").cpf("11111111111").sexo("M").senha("senha1").build();
        UsuarioEntity usuario2 = UsuarioEntity.builder()
                .nome("Usuario Test 2").email("usuario2@test.com").cpf("22222222222").sexo("F").senha("senha2").build();
        this.em.persist(usuario1);
        this.em.persist(usuario2);

        // Cadastra tres atribuicoes, cada uma com uma combinacao unica de usuario e cargo
        AtribuicaoEntity atribuicao1 = AtribuicaoEntity.builder().usuario(usuario1).cargo(cargo1).build();
        AtribuicaoEntity atribuicao2 = AtribuicaoEntity.builder().usuario(usuario2).cargo(cargo2).build();
        AtribuicaoEntity atribuicao3 = AtribuicaoEntity.builder().usuario(usuario2).cargo(cargo3).build();
        this.em.persist(atribuicao1);
        this.em.persist(atribuicao2);
        this.em.persist(atribuicao3);

        return List.of(atribuicao1, atribuicao2, atribuicao3);
    }
}
