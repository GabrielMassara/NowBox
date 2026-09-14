package com.nowbox.nowbox_api.modules.box.repository;

import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

        Page<BoxEntity> result = boxRepository.findAllByFilter(null, boxes.get(0).getNumero(), null, null);

        assertThat(result.getContent().getFirst().getNumero()).isEqualTo(boxes.get(0).getNumero());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return box with disponivel filter")
    void findAllByFilterCase2() {
        List<BoxEntity> boxes = this.createScenario();
        BoxEntity boxIndisponivel = boxes.get(1);

        Page<BoxEntity> result = boxRepository.findAllByFilter(null, null, boxIndisponivel.getDisponivel(), null);

        assertThat(result.getContent().getFirst().getNumero()).isEqualTo(boxIndisponivel.getNumero());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return box with idUnidade filter")
    void findAllByFilterCase3() {
        List<BoxEntity> boxes = this.createScenario();
        BoxEntity boxOutraUnidade = boxes.get(2);

        Page<BoxEntity> result = boxRepository.findAllByFilter(boxOutraUnidade.getUnidade().getId(), null, null, null);

        assertThat(result.getContent().getFirst().getNumero()).isEqualTo(boxOutraUnidade.getNumero());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of box")
    void findAllByFilterCase4() {
        this.createScenario();

        Page<BoxEntity> result = boxRepository.findAllByFilter(null, null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of box")
    void findAllByFilterCase5() {
        Page<BoxEntity> result = boxRepository.findAllByFilter(null, null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of box")
    void findAllByFilterCase6() {
        this.createScenario();

        Page<BoxEntity> result = boxRepository.findAllByFilter(null, "Box Inexistente", null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should not return soft deleted box")
    void findAllByFilterCase7() {
        List<BoxEntity> boxes = this.createScenario();
        BoxEntity box1 = boxes.get(0);
        box1.setDeletedAt(LocalDateTime.now());
        this.em.persist(box1);

        Page<BoxEntity> result = boxRepository.findAllByFilter(null, box1.getNumero(), null, null);

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
