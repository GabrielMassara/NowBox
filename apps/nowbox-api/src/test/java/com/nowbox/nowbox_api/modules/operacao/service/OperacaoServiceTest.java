package com.nowbox.nowbox_api.modules.operacao.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import com.nowbox.nowbox_api.modules.modulo.repository.IModuloRepository;
import com.nowbox.nowbox_api.modules.operacao.dto.OperacaoCreateDTO;
import com.nowbox.nowbox_api.modules.operacao.dto.OperacaoFilterDTO;
import com.nowbox.nowbox_api.modules.operacao.dto.OperacaoResponseDTO;
import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import com.nowbox.nowbox_api.modules.operacao.repository.IOperacaoRepository;
import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperacaoServiceTest {

    @Mock
    private IOperacaoRepository operacaoRepository;

    @Mock
    private IModuloRepository moduloRepository;

    @InjectMocks
    private OperacaoService operacaoService;

    @Test
    @DisplayName("Should list one operacao filtered by idModulo, nome and codigo")
    void listAllByFilterCase1() {
        // id do modulo utilizado no filtro
        UUID idModulo = UUID.randomUUID();
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        ModuloEntity modulo = ModuloEntity.builder().id(idModulo).nome("Modulo").rota("/modulo").sessao(sessao).build();

        // inicializa o filtro
        OperacaoFilterDTO filtro = OperacaoFilterDTO.builder().idModulo(idModulo).nome("Operacao Test").codigo("OP1").build();

        // Mock para simular resposta do Repository
        OperacaoEntity entidade = OperacaoEntity.builder().nome("Operacao Test").codigo("OP1").modulo(modulo).build();
        Page<OperacaoEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(operacaoRepository.findAllByFilter(idModulo, "Operacao Test", "OP1", null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<OperacaoResponseDTO> result = operacaoService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Operacao Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(operacaoRepository).findAllByFilter(idModulo, "Operacao Test", "OP1", null);
    }

    @Test
    @DisplayName("Should list one operacao filtered by nome")
    void listAllByFilterCase2() {
        // inicializa o filtro
        OperacaoFilterDTO filtro = OperacaoFilterDTO.builder().nome("Operacao Test").build();

        // Mock para simular resposta do Repository
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        ModuloEntity modulo = ModuloEntity.builder().nome("Modulo").rota("/modulo").sessao(sessao).build();
        OperacaoEntity entidade = OperacaoEntity.builder().nome("Operacao Test").codigo("OP1").modulo(modulo).build();
        Page<OperacaoEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(operacaoRepository.findAllByFilter(null, "Operacao Test", null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<OperacaoResponseDTO> result = operacaoService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Operacao Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(operacaoRepository).findAllByFilter(null, "Operacao Test", null, null);
    }

    @Test
    @DisplayName("Should list all operacoes when filter is null")
    void listAllByFilterCase3() {
        // Mock para simular resposta do Repository
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        ModuloEntity modulo = ModuloEntity.builder().nome("Modulo").rota("/modulo").sessao(sessao).build();
        OperacaoEntity entidade1 = OperacaoEntity.builder().nome("Operacao Test 1").codigo("OP1").modulo(modulo).build();
        OperacaoEntity entidade2 = OperacaoEntity.builder().nome("Operacao Test 2").codigo("OP2").modulo(modulo).build();
        Page<OperacaoEntity> paginaMock = new PageImpl<>(List.of(entidade1, entidade2));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(operacaoRepository.findAllByFilter(null, null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter passando filtro nulo
        Page<OperacaoResponseDTO> result = operacaoService.listAllByFilter(null, null);

        assertThat(result.getContent()).hasSize(2);

        // verifica se ao chamar a findAllByFilter ele passou todos os parametros nulos
        verify(operacaoRepository).findAllByFilter(null, null, null, null);
    }

    @Test
    @DisplayName("Should not list operacao when filter does not match")
    void listAllByFilterCase4() {
        // inicializa o filtro
        OperacaoFilterDTO filtro = OperacaoFilterDTO.builder().nome("Operacao Inexistente").codigo("NAOEXISTE").build();

        // Mock para simular resposta do Repository
        Page<OperacaoEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(operacaoRepository.findAllByFilter(null, "Operacao Inexistente", "NAOEXISTE", null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<OperacaoResponseDTO> result = operacaoService.listAllByFilter(null, filtro);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(operacaoRepository).findAllByFilter(null, "Operacao Inexistente", "NAOEXISTE", null);
    }

    @Test
    @DisplayName("Should return operacao when id exists")
    void findCase1() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Repository
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        ModuloEntity modulo = ModuloEntity.builder().nome("Modulo").rota("/modulo").sessao(sessao).build();
        OperacaoEntity entidade = OperacaoEntity.builder().id(id).nome("Operacao Test").codigo("OP1").modulo(modulo).build();

        // Quando chamar findById ele retorna o mock entidade
        when(operacaoRepository.findById(id)).thenReturn(Optional.of(entidade));

        // chama a funcao find
        OperacaoResponseDTO result = operacaoService.find(null, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Operacao Test");
        assertThat(result.getCodigo()).isEqualTo("OP1");

        // verifica se ao chamar a findById ele passou o mesmo id
        verify(operacaoRepository).findById(id);
    }

    @Test
    @DisplayName("Should throw exception when operacao id does not exist")
    void findCase2() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar findById ele retorna vazio
        when(operacaoRepository.findById(id)).thenReturn(Optional.empty());

        // chama a funcao find e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> operacaoService.find(null, id));

        // verifica se ao chamar a findById ele passou o mesmo id
        verify(operacaoRepository).findById(id);
    }

    @Test
    @DisplayName("Should save and return created operacao")
    void createCase1() {
        // id do modulo utilizado na criacao
        UUID idModulo = UUID.randomUUID();
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        ModuloEntity modulo = ModuloEntity.builder().id(idModulo).nome("Modulo").rota("/modulo").sessao(sessao).build();

        // dados para criacao
        OperacaoCreateDTO operacao = OperacaoCreateDTO.builder().idModulo(idModulo).nome("Operacao Test").codigo("OP1").build();

        // Mock para simular que o modulo existe
        when(moduloRepository.findById(idModulo)).thenReturn(Optional.of(modulo));

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        OperacaoEntity entidadeSalva = OperacaoEntity.builder().id(id).nome("Operacao Test").codigo("OP1").modulo(modulo).build();
        when(operacaoRepository.save(any(OperacaoEntity.class))).thenReturn(entidadeSalva);

        // chama a funcao create
        OperacaoResponseDTO result = operacaoService.create(operacao);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Operacao Test");
        assertThat(result.getCodigo()).isEqualTo("OP1");
        assertThat(result.getModulo()).isEqualTo(modulo);

        // verifica se buscou o modulo antes de criar
        verify(moduloRepository).findById(idModulo);

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(operacaoRepository).save(argThat(e -> e.getNome().equals("Operacao Test") && e.getCodigo().equals("OP1") && e.getModulo().equals(modulo)));
    }

    @Test
    @DisplayName("Should throw exception when creating operacao with a modulo that does not exist")
    void createCase2() {
        // id do modulo inexistente
        UUID idModulo = UUID.randomUUID();

        // dados para criacao
        OperacaoCreateDTO operacao = OperacaoCreateDTO.builder().idModulo(idModulo).nome("Operacao Test").codigo("OP1").build();

        // Mock para simular que o modulo nao existe
        when(moduloRepository.findById(idModulo)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> operacaoService.create(operacao));

        // verifica se buscou o modulo antes de tentar criar
        verify(moduloRepository).findById(idModulo);

        // verifica se nunca chegou a salvar, ja que o modulo nao existe
        verify(operacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update operacao when id and modulo exist")
    void updateCase1() {
        // id da operacao e do modulo utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idModulo = UUID.randomUUID();

        // dados para atualizacao
        OperacaoCreateDTO operacao = OperacaoCreateDTO.builder().idModulo(idModulo).nome("Operacao Test").codigo("OP1").build();

        // Mock para simular que a operacao existe
        SessaoEntity sessaoAntiga = SessaoEntity.builder().nome("Sessao Old").rota("/old").build();
        ModuloEntity moduloAntigo = ModuloEntity.builder().nome("Modulo Old").rota("/old").sessao(sessaoAntiga).build();
        OperacaoEntity entidadeExistente = OperacaoEntity.builder().id(id).nome("Operacao Old").codigo("OPOLD").modulo(moduloAntigo).build();
        when(operacaoRepository.findById(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o modulo existe
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        ModuloEntity modulo = ModuloEntity.builder().id(idModulo).nome("Modulo").rota("/modulo").sessao(sessao).build();
        when(moduloRepository.findById(idModulo)).thenReturn(Optional.of(modulo));

        // Mock para simular resposta do save
        OperacaoEntity entidadeAtualizada = OperacaoEntity.builder().id(id).nome("Operacao Test").codigo("OP1").modulo(modulo).build();
        when(operacaoRepository.save(any(OperacaoEntity.class))).thenReturn(entidadeAtualizada);

        // chama a funcao update
        OperacaoResponseDTO result = operacaoService.update(operacao, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Operacao Test");
        assertThat(result.getCodigo()).isEqualTo("OP1");
        assertThat(result.getModulo()).isEqualTo(modulo);

        // verifica se buscou a operacao e o modulo antes de atualizar
        verify(operacaoRepository).findById(id);
        verify(moduloRepository).findById(idModulo);

        // verifica se salvou a entidade com os dados atualizados
        verify(operacaoRepository).save(argThat(e -> e.getId().equals(id) && e.getNome().equals("Operacao Test") && e.getCodigo().equals("OP1") && e.getModulo().equals(modulo)));
    }

    @Test
    @DisplayName("Should throw exception when updating a operacao that does not exist")
    void updateCase2() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        OperacaoCreateDTO operacao = OperacaoCreateDTO.builder().idModulo(UUID.randomUUID()).nome("Operacao Test").codigo("OP1").build();

        // Mock para simular que a operacao nao existe
        when(operacaoRepository.findById(id)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> operacaoService.update(operacao, id));

        // verifica se buscou a operacao antes de tentar atualizar
        verify(operacaoRepository).findById(id);

        // verifica se nunca chegou a buscar o modulo nem a salvar
        verify(moduloRepository, never()).findById(any(UUID.class));
        verify(operacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating operacao with a modulo that does not exist")
    void updateCase3() {
        // id da operacao e do modulo utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idModulo = UUID.randomUUID();
        OperacaoCreateDTO operacao = OperacaoCreateDTO.builder().idModulo(idModulo).nome("Operacao Test").codigo("OP1").build();

        // Mock para simular que a operacao existe
        SessaoEntity sessaoAntiga = SessaoEntity.builder().nome("Sessao Old").rota("/old").build();
        ModuloEntity moduloAntigo = ModuloEntity.builder().nome("Modulo Old").rota("/old").sessao(sessaoAntiga).build();
        OperacaoEntity entidadeExistente = OperacaoEntity.builder().id(id).nome("Operacao Old").codigo("OPOLD").modulo(moduloAntigo).build();
        when(operacaoRepository.findById(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o modulo nao existe
        when(moduloRepository.findById(idModulo)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> operacaoService.update(operacao, id));

        // verifica se buscou a operacao e o modulo antes de tentar atualizar
        verify(operacaoRepository).findById(id);
        verify(moduloRepository).findById(idModulo);

        // verifica se nunca chegou a salvar, ja que o modulo nao existe
        verify(operacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete operacao by id")
    void deleteCase1() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // chama a funcao delete
        operacaoService.delete(id);

        // verifica se excluiu o id correto
        verify(operacaoRepository).deleteById(id);
    }
}
