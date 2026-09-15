package com.nowbox.nowbox_api.modules.cliente.repository;

import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class IClienteRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    IClienteRepository clienteRepository;

    @Test
    @DisplayName("Return cliente with nome filter")
    void findAllByFilterCase1() {
        List<ClienteEntity> clientes = this.createScenario();

        Page<ClienteEntity> result = clienteRepository.findAllByFilter(null, clientes.get(0).getNome(), null, null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(clientes.get(0).getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return cliente with cpf filter")
    void findAllByFilterCase2() {
        List<ClienteEntity> clientes = this.createScenario();
        ClienteEntity cliente2 = clientes.get(1);

        Page<ClienteEntity> result = clienteRepository.findAllByFilter(null, null, cliente2.getCpf(), null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(cliente2.getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return cliente with email filter")
    void findAllByFilterCase3() {
        List<ClienteEntity> clientes = this.createScenario();
        ClienteEntity cliente1 = clientes.get(0);

        Page<ClienteEntity> result = clienteRepository.findAllByFilter(null, null, null, cliente1.getEmail(), null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(cliente1.getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return cliente with idEstado filter")
    void findAllByFilterCase4() {
        List<ClienteEntity> clientes = this.createScenario();
        ClienteEntity clienteOutroEstado = clientes.get(2);

        Page<ClienteEntity> result = clienteRepository.findAllByFilter(clienteOutroEstado.getEstado().getId(), null, null, null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(clienteOutroEstado.getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of cliente")
    void findAllByFilterCase5() {
        this.createScenario();

        Page<ClienteEntity> result = clienteRepository.findAllByFilter(null, null, null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of cliente")
    void findAllByFilterCase6() {
        Page<ClienteEntity> result = clienteRepository.findAllByFilter(null, null, null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of cliente")
    void findAllByFilterCase7() {
        this.createScenario();

        Page<ClienteEntity> result = clienteRepository.findAllByFilter(null, "Cliente Inexistente", null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should not return soft deleted cliente")
    void findAllByFilterCase8() {
        List<ClienteEntity> clientes = this.createScenario();
        ClienteEntity cliente1 = clientes.get(0);
        cliente1.setDeletedAt(LocalDateTime.now());
        this.em.persist(cliente1);

        Page<ClienteEntity> result = clienteRepository.findAllByFilter(null, cliente1.getNome(), null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should return cliente when id exists and is not deleted")
    void findByIdAndDeletedAtIsNullCase1() {
        List<ClienteEntity> clientes = this.createScenario();
        ClienteEntity cliente1 = clientes.get(0);

        Optional<ClienteEntity> result = clienteRepository.findByIdAndDeletedAtIsNull(cliente1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getNome()).isEqualTo(cliente1.getNome());
    }

    @Test
    @DisplayName("Should not return cliente when id is soft deleted")
    void findByIdAndDeletedAtIsNullCase2() {
        List<ClienteEntity> clientes = this.createScenario();
        ClienteEntity cliente1 = clientes.get(0);
        cliente1.setDeletedAt(LocalDateTime.now());
        this.em.persist(cliente1);

        Optional<ClienteEntity> result = clienteRepository.findByIdAndDeletedAtIsNull(cliente1.getId());

        assertThat(result).isEmpty();
    }

    private List<ClienteEntity> createScenario() {
        // Cadastra um estado de teste
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("XX").build();
        this.em.persist(estado);

        // Cadastra dois clientes no mesmo estado
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

        // Cadastra um segundo estado com um cliente para testar o filtro por idEstado
        EstadoEntity outroEstado = EstadoEntity.builder().nome("Estado 2").uf("YY").build();
        this.em.persist(outroEstado);

        ClienteEntity cliente3 = ClienteEntity.builder()
                .nome("Cliente 3").profissao("Medico").cpf("33333333333").rg("333333333")
                .email("cliente3@test.com").telefone("11933333333").sexo("M")
                .nascimento(LocalDate.of(1992, 3, 3)).endereco("Rua 3").numero("3")
                .bairro("Bairro 3").cep("33333333").cidade("Cidade 3").estado(outroEstado)
                .enderecoCorrespondencia(true).senha("senha789").senhaTemporariaStatus(false).build();
        this.em.persist(cliente3);

        return List.of(cliente1, cliente2, cliente3);
    }
}
