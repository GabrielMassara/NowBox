package com.nowbox.nowbox_api.modules.aluguel.repository;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class IAluguelRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    IAluguelRepository aluguelRepository;

    @Test
    @DisplayName("Return aluguel with idBox filter")
    void findAllByFilterCase1() {
        List<AluguelEntity> alugueis = this.createScenario();

        Page<AluguelEntity> result = aluguelRepository.findAllByFilter(alugueis.get(0).getBox().getId(), null, null);

        assertThat(result.getContent().getFirst().getId()).isEqualTo(alugueis.get(0).getId());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return aluguel with idCliente filter")
    void findAllByFilterCase2() {
        List<AluguelEntity> alugueis = this.createScenario();
        AluguelEntity aluguelOutroCliente = alugueis.get(2);

        Page<AluguelEntity> result = aluguelRepository.findAllByFilter(null, aluguelOutroCliente.getCliente().getId(), null);

        assertThat(result.getContent().getFirst().getId()).isEqualTo(aluguelOutroCliente.getId());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of aluguel")
    void findAllByFilterCase3() {
        this.createScenario();

        Page<AluguelEntity> result = aluguelRepository.findAllByFilter(null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of aluguel")
    void findAllByFilterCase4() {
        Page<AluguelEntity> result = aluguelRepository.findAllByFilter(null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of aluguel")
    void findAllByFilterCase5() {
        this.createScenario();

        Page<AluguelEntity> result = aluguelRepository.findAllByFilter(UUID.randomUUID(), UUID.randomUUID(), null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Full filter match: Should return one element of aluguel")
    void findAllByFilterCase6() {
        List<AluguelEntity> alugueis = this.createScenario();
        AluguelEntity aluguel1 = alugueis.get(0);

        Page<AluguelEntity> result = aluguelRepository.findAllByFilter(
                aluguel1.getBox().getId(), aluguel1.getCliente().getId(), null);

        assertThat(result.getContent().getFirst().getId()).isEqualTo(aluguel1.getId());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Filter mismatch between idBox and idCliente: Should not return elements of aluguel")
    void findAllByFilterCase7() {
        List<AluguelEntity> alugueis = this.createScenario();
        AluguelEntity aluguel1 = alugueis.get(0);
        AluguelEntity aluguelOutroCliente = alugueis.get(2);

        Page<AluguelEntity> result = aluguelRepository.findAllByFilter(
                aluguel1.getBox().getId(), aluguelOutroCliente.getCliente().getId(), null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should not return soft deleted aluguel")
    void findAllByFilterCase8() {
        List<AluguelEntity> alugueis = this.createScenario();
        AluguelEntity aluguel1 = alugueis.get(0);
        aluguel1.setDeletedAt(LocalDateTime.now());
        this.em.persist(aluguel1);

        Page<AluguelEntity> result = aluguelRepository.findAllByFilter(aluguel1.getBox().getId(), null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should return aluguel when id exists and is not deleted")
    void findByIdAndDeletedAtIsNullCase1() {
        List<AluguelEntity> alugueis = this.createScenario();
        AluguelEntity aluguel1 = alugueis.get(0);

        Optional<AluguelEntity> result = aluguelRepository.findByIdAndDeletedAtIsNull(aluguel1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(aluguel1.getId());
    }

    @Test
    @DisplayName("Should not return aluguel when id is soft deleted")
    void findByIdAndDeletedAtIsNullCase2() {
        List<AluguelEntity> alugueis = this.createScenario();
        AluguelEntity aluguel1 = alugueis.get(0);
        aluguel1.setDeletedAt(LocalDateTime.now());
        this.em.persist(aluguel1);

        Optional<AluguelEntity> result = aluguelRepository.findByIdAndDeletedAtIsNull(aluguel1.getId());

        assertThat(result).isEmpty();
    }

    private List<AluguelEntity> createScenario() {
        // Cadastra um estado e uma unidade de teste
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("XX").build();
        this.em.persist(estado);

        UnidadeEntity unidade = UnidadeEntity.builder()
                .nome("Unidade").cnpj("11111111111111").endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(estado).build();
        this.em.persist(unidade);

        // Cadastra tres boxes de teste
        BoxEntity box1 = BoxEntity.builder().numero("101").unidade(unidade).tamanho(BigDecimal.valueOf(10.5)).dimensoes("2x5").disponivel(true).preco(BigDecimal.valueOf(150.00)).build();
        BoxEntity box2 = BoxEntity.builder().numero("102").unidade(unidade).tamanho(BigDecimal.valueOf(10.5)).dimensoes("2x5").disponivel(true).preco(BigDecimal.valueOf(150.00)).build();
        BoxEntity box3 = BoxEntity.builder().numero("103").unidade(unidade).tamanho(BigDecimal.valueOf(10.5)).dimensoes("2x5").disponivel(true).preco(BigDecimal.valueOf(150.00)).build();
        this.em.persist(box1);
        this.em.persist(box2);
        this.em.persist(box3);

        // Cadastra dois clientes de teste
        ClienteEntity cliente1 = ClienteEntity.builder()
                .nome("Cliente 1").profissao("Engenheiro").cpf("11111111111").rg("111111111")
                .email("cliente1@test.com").telefone("11911111111").sexo("M")
                .nascimento(LocalDate.of(1990, 1, 1)).endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(estado)
                .enderecoCorrespondencia(true).senha("senha123").senhaTemporariaStatus(false).build();
        ClienteEntity cliente2 = ClienteEntity.builder()
                .nome("Cliente 2").profissao("Advogado").cpf("22222222222").rg("222222222")
                .email("cliente2@test.com").telefone("11922222222").sexo("F")
                .nascimento(LocalDate.of(1991, 2, 2)).endereco("Rua 2").numero("2")
                .bairro("Bairro 2").cep("22222222").cidade("Cidade 2").estado(estado)
                .enderecoCorrespondencia(false).senha("senha456").senhaTemporariaStatus(false).build();
        this.em.persist(cliente1);
        this.em.persist(cliente2);

        // Cadastra tres alugueis, cada um com uma combinacao unica de box e cliente
        AluguelEntity aluguel1 = AluguelEntity.builder().box(box1).cliente(cliente1).valor(BigDecimal.valueOf(150.00)).observacao("Aluguel 1").build();
        AluguelEntity aluguel2 = AluguelEntity.builder().box(box2).cliente(cliente1).valor(BigDecimal.valueOf(180.00)).observacao("Aluguel 2").build();
        AluguelEntity aluguel3 = AluguelEntity.builder().box(box3).cliente(cliente2).valor(BigDecimal.valueOf(200.00)).observacao("Aluguel 3").build();
        this.em.persist(aluguel1);
        this.em.persist(aluguel2);
        this.em.persist(aluguel3);

        return List.of(aluguel1, aluguel2, aluguel3);
    }
}
