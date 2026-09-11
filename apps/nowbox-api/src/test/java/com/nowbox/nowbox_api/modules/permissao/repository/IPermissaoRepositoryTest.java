package com.nowbox.nowbox_api.modules.permissao.repository;

import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import com.nowbox.nowbox_api.modules.permissao.entity.PermissaoEntity;
import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class IPermissaoRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    IPermissaoRepository permissaoRepository;

    @Test
    @DisplayName("Return permissao with idCargo filter")
    void findAllByFilterCase1() {
        List<PermissaoEntity> permissoes = this.createScenario();

        Page<PermissaoEntity> result = permissaoRepository.findAllByFilter(permissoes.get(0).getCargo().getId(), null, null);

        assertThat(result.getContent().getFirst().getId()).isEqualTo(permissoes.get(0).getId());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return permissao with idOperacao filter")
    void findAllByFilterCase2() {
        List<PermissaoEntity> permissoes = this.createScenario();
        PermissaoEntity permissaoOutraOperacao = permissoes.get(2);

        Page<PermissaoEntity> result = permissaoRepository.findAllByFilter(null, permissaoOutraOperacao.getOperacao().getId(), null);

        assertThat(result.getContent().getFirst().getId()).isEqualTo(permissaoOutraOperacao.getId());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of permissao")
    void findAllByFilterCase3() {
        this.createScenario();

        Page<PermissaoEntity> result = permissaoRepository.findAllByFilter(null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of permissao")
    void findAllByFilterCase4() {
        Page<PermissaoEntity> result = permissaoRepository.findAllByFilter(null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of permissao")
    void findAllByFilterCase5() {
        this.createScenario();

        Page<PermissaoEntity> result = permissaoRepository.findAllByFilter(UUID.randomUUID(), UUID.randomUUID(), null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Full filter match: Should return one element of permissao")
    void findAllByFilterCase6() {
        List<PermissaoEntity> permissoes = this.createScenario();
        PermissaoEntity permissao1 = permissoes.get(0);

        Page<PermissaoEntity> result = permissaoRepository.findAllByFilter(
                permissao1.getCargo().getId(), permissao1.getOperacao().getId(), null);

        assertThat(result.getContent().getFirst().getId()).isEqualTo(permissao1.getId());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Filter mismatch between idCargo and idOperacao: Should not return elements of permissao")
    void findAllByFilterCase7() {
        List<PermissaoEntity> permissoes = this.createScenario();
        PermissaoEntity permissao1 = permissoes.get(0);
        PermissaoEntity permissaoOutraOperacao = permissoes.get(2);

        Page<PermissaoEntity> result = permissaoRepository.findAllByFilter(
                permissao1.getCargo().getId(), permissaoOutraOperacao.getOperacao().getId(), null);

        assertThat(result.getContent()).isEmpty();
    }

    private List<PermissaoEntity> createScenario() {
        // Cadastra um estado, uma unidade e dois cargos de teste
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("XX").build();
        this.em.persist(estado);

        UnidadeEntity unidade = UnidadeEntity.builder()
                .nome("Unidade").cnpj("11111111111111").endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(estado).build();
        this.em.persist(unidade);

        CargoEntity cargo1 = CargoEntity.builder().nome("Cargo Test 1").unidade(unidade).build();
        CargoEntity cargo2 = CargoEntity.builder().nome("Cargo Test 2").unidade(unidade).build();
        this.em.persist(cargo1);
        this.em.persist(cargo2);

        // Cadastra uma sessao, um modulo e tres operacoes de teste
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        this.em.persist(sessao);

        ModuloEntity modulo = ModuloEntity.builder().nome("Modulo").rota("/modulo").sessao(sessao).build();
        this.em.persist(modulo);

        OperacaoEntity operacao1 = OperacaoEntity.builder().nome("Operacao Test 1").codigo("OP1").modulo(modulo).build();
        OperacaoEntity operacao2 = OperacaoEntity.builder().nome("Operacao Test 2").codigo("OP2").modulo(modulo).build();
        OperacaoEntity operacao3 = OperacaoEntity.builder().nome("Operacao Test 3").codigo("OP3").modulo(modulo).build();
        this.em.persist(operacao1);
        this.em.persist(operacao2);
        this.em.persist(operacao3);

        // Cadastra tres permissoes, cada uma com uma combinacao unica de cargo e operacao
        PermissaoEntity permissao1 = PermissaoEntity.builder().cargo(cargo1).operacao(operacao1).build();
        PermissaoEntity permissao2 = PermissaoEntity.builder().cargo(cargo2).operacao(operacao2).build();
        PermissaoEntity permissao3 = PermissaoEntity.builder().cargo(cargo2).operacao(operacao3).build();
        this.em.persist(permissao1);
        this.em.persist(permissao2);
        this.em.persist(permissao3);

        return List.of(permissao1, permissao2, permissao3);
    }
}
