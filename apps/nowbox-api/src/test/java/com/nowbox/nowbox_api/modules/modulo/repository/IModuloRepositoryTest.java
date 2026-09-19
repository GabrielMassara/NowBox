package com.nowbox.nowbox_api.modules.modulo.repository;

import com.nowbox.nowbox_api.modules.atribuicao.entity.AtribuicaoEntity;
import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import com.nowbox.nowbox_api.modules.permissao.entity.PermissaoEntity;
import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
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
import java.util.UUID;

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

    @Test
    @DisplayName("Should return only the modulos that have a permitted operacao for the usuario, ordered by sessao and modulo")
    void findAllByUsuarioCase1() {
        UsuarioEntity usuario = this.createUsuarioScenario(null);

        List<ModuloEntity> result = moduloRepository.findAllByUsuario(usuario.getId());

        // Somente os modulos A1 e B1 possuem permissao
        assertThat(result).extracting(ModuloEntity::getNome).containsExactly("Modulo A1", "Modulo B1");
        assertThat(result).extracting(m -> m.getSessao().getNome()).containsExactly("Sessao A", "Sessao B");
    }

    @Test
    @DisplayName("Should return the modulo only once when the usuario has more than one permitted operacao in it")
    void findAllByUsuarioCase2() {
        UsuarioEntity usuario = this.createUsuarioScenario(null);

        List<ModuloEntity> result = moduloRepository.findAllByUsuario(usuario.getId());

        // Modulo A1 possui duas operacoes permitidas mas aparece uma vez
        assertThat(result.stream().filter(m -> m.getNome().equals("Modulo A1"))).hasSize(1);
    }

    @Test
    @DisplayName("Should not return modulos when the atribuicao is soft deleted")
    void findAllByUsuarioCase3() {
        UsuarioEntity usuario = this.createUsuarioScenario(LocalDateTime.now());

        List<ModuloEntity> result = moduloRepository.findAllByUsuario(usuario.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when usuario has no atribuicao")
    void findAllByUsuarioCase4() {
        this.createUsuarioScenario(null);

        List<ModuloEntity> result = moduloRepository.findAllByUsuario(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    // Cria sessoes e modulos com permissao para o usuario
    private UsuarioEntity createUsuarioScenario(LocalDateTime deletedAtAtribuicao) {
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("XX").build();
        this.em.persist(estado);

        UnidadeEntity unidade = UnidadeEntity.builder()
                .nome("Unidade").cnpj("11111111111111").endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(estado).build();
        this.em.persist(unidade);

        CargoEntity cargo = CargoEntity.builder().nome("Cargo Test").unidade(unidade).build();
        this.em.persist(cargo);

        // Sessoes fora de ordem alfabetica para validar a ordenacao
        SessaoEntity sessaoB = SessaoEntity.builder().nome("Sessao B").rota("/sessaoB").build();
        SessaoEntity sessaoA = SessaoEntity.builder().nome("Sessao A").rota("/sessaoA").build();
        SessaoEntity sessaoC = SessaoEntity.builder().nome("Sessao C").rota("/sessaoC").build();
        this.em.persist(sessaoB);
        this.em.persist(sessaoA);
        this.em.persist(sessaoC);

        ModuloEntity moduloB1 = ModuloEntity.builder().nome("Modulo B1").rota("/moduloB1").sessao(sessaoB).build();
        ModuloEntity moduloA1 = ModuloEntity.builder().nome("Modulo A1").rota("/moduloA1").sessao(sessaoA).build();
        ModuloEntity moduloA2 = ModuloEntity.builder().nome("Modulo A2").rota("/moduloA2").sessao(sessaoA).build();
        ModuloEntity moduloC1 = ModuloEntity.builder().nome("Modulo C1").rota("/moduloC1").sessao(sessaoC).build();
        this.em.persist(moduloB1);
        this.em.persist(moduloA1);
        this.em.persist(moduloA2);
        this.em.persist(moduloC1);

        OperacaoEntity opB1 = OperacaoEntity.builder().nome("Consultar B1").codigo("B1_CONSULTAR").modulo(moduloB1).build();
        OperacaoEntity opA1Consultar = OperacaoEntity.builder().nome("Consultar A1").codigo("A1_CONSULTAR").modulo(moduloA1).build();
        OperacaoEntity opA1Cadastrar = OperacaoEntity.builder().nome("Cadastrar A1").codigo("A1_CADASTRAR").modulo(moduloA1).build();
        OperacaoEntity opA2 = OperacaoEntity.builder().nome("Consultar A2").codigo("A2_CONSULTAR").modulo(moduloA2).build();
        OperacaoEntity opC1 = OperacaoEntity.builder().nome("Consultar C1").codigo("C1_CONSULTAR").modulo(moduloC1).build();
        this.em.persist(opB1);
        this.em.persist(opA1Consultar);
        this.em.persist(opA1Cadastrar);
        this.em.persist(opA2);
        this.em.persist(opC1);

        // O cargo so possui permissao em B1 e A1
        this.em.persist(PermissaoEntity.builder().cargo(cargo).operacao(opB1).build());
        this.em.persist(PermissaoEntity.builder().cargo(cargo).operacao(opA1Consultar).build());
        this.em.persist(PermissaoEntity.builder().cargo(cargo).operacao(opA1Cadastrar).build());

        UsuarioEntity usuario = UsuarioEntity.builder()
                .nome("Usuario Test").email("usuario@test.com").cpf("11111111111").sexo("M").senha("senha1").build();
        this.em.persist(usuario);

        this.em.persist(AtribuicaoEntity.builder().usuario(usuario).cargo(cargo).deletedAt(deletedAtAtribuicao).build());

        return usuario;
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