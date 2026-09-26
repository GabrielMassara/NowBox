package com.nowbox.nowbox_api.modules.box.repository;

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
class IBoxRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    IBoxRepository boxRepository;

    @Test
    @DisplayName("Return box with numero filter")
    void findAllByFilterCase1() {
        List<BoxEntity> boxes = this.createScenario();

        Page<BoxEntity> result = boxRepository.findAllByFilter(null, boxes.get(0).getNumero(), null, null, null);

        assertThat(result.getContent().getFirst().getNumero()).isEqualTo(boxes.get(0).getNumero());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return box with disponivel filter")
    void findAllByFilterCase2() {
        List<BoxEntity> boxes = this.createScenario();
        BoxEntity boxIndisponivel = boxes.get(1);

        Page<BoxEntity> result = boxRepository.findAllByFilter(null, null, boxIndisponivel.getDisponivel(), null, null);

        assertThat(result.getContent().getFirst().getNumero()).isEqualTo(boxIndisponivel.getNumero());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return box with idUnidade filter")
    void findAllByFilterCase3() {
        List<BoxEntity> boxes = this.createScenario();
        BoxEntity boxOutraUnidade = boxes.get(2);

        Page<BoxEntity> result = boxRepository.findAllByFilter(boxOutraUnidade.getUnidade().getId(), null, null, null, null);

        assertThat(result.getContent().getFirst().getNumero()).isEqualTo(boxOutraUnidade.getNumero());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of box")
    void findAllByFilterCase4() {
        this.createScenario();

        Page<BoxEntity> result = boxRepository.findAllByFilter(null, null, null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of box")
    void findAllByFilterCase5() {
        Page<BoxEntity> result = boxRepository.findAllByFilter(null, null, null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of box")
    void findAllByFilterCase6() {
        this.createScenario();

        Page<BoxEntity> result = boxRepository.findAllByFilter(null, "Box Inexistente", null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should not return soft deleted box")
    void findAllByFilterCase7() {
        List<BoxEntity> boxes = this.createScenario();
        BoxEntity box1 = boxes.get(0);
        box1.setDeletedAt(LocalDateTime.now());
        this.em.persist(box1);

        Page<BoxEntity> result = boxRepository.findAllByFilter(null, box1.getNumero(), null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should return box when id exists and is not deleted")
    void findByIdAndDeletedAtIsNullCase1() {
        List<BoxEntity> boxes = this.createScenario();
        BoxEntity box1 = boxes.get(0);

        Optional<BoxEntity> result = boxRepository.findByIdAndDeletedAtIsNull(box1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getNumero()).isEqualTo(box1.getNumero());
    }

    @Test
    @DisplayName("Should not return box when id is soft deleted")
    void findByIdAndDeletedAtIsNullCase2() {
        List<BoxEntity> boxes = this.createScenario();
        BoxEntity box1 = boxes.get(0);
        box1.setDeletedAt(LocalDateTime.now());
        this.em.persist(box1);

        Optional<BoxEntity> result = boxRepository.findByIdAndDeletedAtIsNull(box1.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Return only boxes without an active aluguel when alugado is false")
    void findAllByFilterCase8() {
        List<BoxEntity> boxes = this.createScenario();
        this.createAlugueis(boxes.get(0), boxes.get(1));
        UUID idUnidade = boxes.get(0).getUnidade().getId();

        // o box 1 tem aluguel ativo, o box 2 so tem aluguel inativo
        Page<BoxEntity> result = boxRepository.findAllByFilter(idUnidade, null, null, false, null);

        assertThat(result.getContent()).extracting(BoxEntity::getNumero).containsExactly("102");
    }

    @Test
    @DisplayName("Return only boxes with an active aluguel when alugado is true")
    void findAllByFilterCase9() {
        List<BoxEntity> boxes = this.createScenario();
        this.createAlugueis(boxes.get(0), boxes.get(1));
        UUID idUnidade = boxes.get(0).getUnidade().getId();

        Page<BoxEntity> result = boxRepository.findAllByFilter(idUnidade, null, null, true, null);

        assertThat(result.getContent()).extracting(BoxEntity::getNumero).containsExactly("101");
    }

    @Test
    @DisplayName("Should not consider a soft deleted aluguel as an active one")
    void findAllByFilterCase10() {
        List<BoxEntity> boxes = this.createScenario();
        List<AluguelEntity> alugueis = this.createAlugueis(boxes.get(0), boxes.get(1));
        AluguelEntity ativo = alugueis.get(0);
        ativo.setDeletedAt(LocalDateTime.now());
        this.em.persist(ativo);
        UUID idUnidade = boxes.get(0).getUnidade().getId();

        Page<BoxEntity> result = boxRepository.findAllByFilter(idUnidade, null, null, false, null);

        assertThat(result.getContent()).extracting(BoxEntity::getNumero).containsExactlyInAnyOrder("101", "102");
    }

    @Test
    @DisplayName("Return only released boxes that are not rented when disponivel and alugado are combined")
    void findAllByFilterCase11() {
        List<BoxEntity> boxes = this.createScenario();
        this.createAlugueis(boxes.get(0), boxes.get(1));
        UUID idUnidade = boxes.get(0).getUnidade().getId();

        // o box 2 esta sem aluguel ativo, mas esta bloqueado (disponivel = false)
        Page<BoxEntity> result = boxRepository.findAllByFilter(idUnidade, null, true, false, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Return only the numeros already registered in the unidade, ignoring the case")
    void findNumerosCadastradosCase1() {
        List<BoxEntity> boxes = this.createScenario();
        BoxEntity box1 = boxes.get(0);

        // 101 existe na unidade; 999 nao existe; 201 existe, mas em outra unidade
        List<String> result = boxRepository.findNumerosCadastrados(box1.getUnidade().getId(), List.of("101", "999", "201"));

        assertThat(result).containsExactly("101");
    }

    @Test
    @DisplayName("Should not return numero of a soft deleted box")
    void findNumerosCadastradosCase2() {
        List<BoxEntity> boxes = this.createScenario();
        BoxEntity box1 = boxes.get(0);
        box1.setDeletedAt(LocalDateTime.now());
        this.em.persist(box1);

        List<String> result = boxRepository.findNumerosCadastrados(box1.getUnidade().getId(), List.of("101", "102"));

        assertThat(result).containsExactly("102");
    }

    // Cadastra um aluguel ativo no primeiro box e um aluguel inativo no segundo
    private List<AluguelEntity> createAlugueis(BoxEntity boxAtivo, BoxEntity boxInativo) {
        ClienteEntity cliente = ClienteEntity.builder()
                .nome("Cliente").profissao("Engenheiro").cpf("11111111111").rg("111111111")
                .email("cliente@test.com").telefone("11911111111").sexo("M")
                .nascimento(LocalDate.of(1990, 1, 1)).endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(boxAtivo.getUnidade().getEstado())
                .enderecoCorrespondencia(true).senha("senha123").senhaTemporariaStatus(false).build();
        this.em.persist(cliente);

        AluguelEntity ativo = AluguelEntity.builder().box(boxAtivo).cliente(cliente).valor(BigDecimal.valueOf(150.00)).status(true).build();
        AluguelEntity inativo = AluguelEntity.builder().box(boxInativo).cliente(cliente).valor(BigDecimal.valueOf(150.00)).status(false).build();
        this.em.persist(ativo);
        this.em.persist(inativo);

        return List.of(ativo, inativo);
    }

    private List<BoxEntity> createScenario() {
        // Cadastra um estado e uma unidade de teste
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("XX").build();
        this.em.persist(estado);

        UnidadeEntity unidade = UnidadeEntity.builder()
                .nome("Unidade Test").cnpj("11111111111111").endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(estado).build();
        this.em.persist(unidade);

        // Cadastra dois boxes na mesma unidade
        BoxEntity box1 = BoxEntity.builder()
                .numero("101").unidade(unidade).tamanho(BigDecimal.valueOf(10.5))
                .dimensoes("2x5").disponivel(true).preco(BigDecimal.valueOf(150.00)).build();
        BoxEntity box2 = BoxEntity.builder()
                .numero("102").unidade(unidade).tamanho(BigDecimal.valueOf(20.0))
                .dimensoes("4x5").disponivel(false).preco(BigDecimal.valueOf(250.00)).build();

        this.em.persist(box1);
        this.em.persist(box2);

        // Cadastra uma segunda unidade com um box para testar o filtro por idUnidade
        UnidadeEntity outraUnidade = UnidadeEntity.builder()
                .nome("Unidade Test 2").cnpj("22222222222222").endereco("Rua 2").numero("2")
                .bairro("Bairro 2").cep("22222222").cidade("Cidade 2").estado(estado).build();
        this.em.persist(outraUnidade);

        BoxEntity box3 = BoxEntity.builder()
                .numero("201").unidade(outraUnidade).tamanho(BigDecimal.valueOf(15.0))
                .dimensoes("3x5").disponivel(true).preco(BigDecimal.valueOf(180.00)).build();
        this.em.persist(box3);

        return List.of(box1, box2, box3);
    }
}
