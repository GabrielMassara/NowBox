package com.nowbox.nowbox_api.modules.modulo.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.modulo.dto.ModuloCreateDTO;
import com.nowbox.nowbox_api.modules.modulo.dto.ModuloFilterDTO;
import com.nowbox.nowbox_api.modules.modulo.dto.ModuloResponseDTO;
import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import com.nowbox.nowbox_api.modules.modulo.repository.IModuloRepository;
import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
import com.nowbox.nowbox_api.modules.sessao.repository.ISessaoRepository;
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
class ModuloServiceTest {

    @Mock
    private IModuloRepository moduloRepository;

    @Mock
    private ISessaoRepository sessaoRepository;

    @InjectMocks
    private ModuloService moduloService;

    @Test
    @DisplayName("Should list one modulo filtered by idSessao, nome and rota")
    void listAllByFilterCase1() {
        // id da sessao utilizado no filtro
        UUID idSessao = UUID.randomUUID();
        SessaoEntity sessao = SessaoEntity.builder().id(idSessao).nome("Sessao").rota("/rotaSessao").build();

        // inicializa o filtro
        ModuloFilterDTO filtro = ModuloFilterDTO.builder().idSessao(idSessao).nome("Modulo Test").rota("/modulo").build();

        // Mock para simular resposta do Repository
        ModuloEntity entidade = ModuloEntity.builder().nome("Modulo Test").rota("/modulo").sessao(sessao).build();
        Page<ModuloEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(moduloRepository.findAllByFilter(idSessao, "Modulo Test", "/modulo", null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<ModuloResponseDTO> result = moduloService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Modulo Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(moduloRepository).findAllByFilter(idSessao, "Modulo Test", "/modulo", null);
    }

    @Test
    @DisplayName("Should list one modulo filtered by nome")
    void listAllByFilterCase2() {
        // inicializa o filtro
        ModuloFilterDTO filtro = ModuloFilterDTO.builder().nome("Modulo Test").build();

        // Mock para simular resposta do Repository
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        ModuloEntity entidade = ModuloEntity.builder().nome("Modulo Test").rota("/modulo").sessao(sessao).build();
        Page<ModuloEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(moduloRepository.findAllByFilter(null, "Modulo Test", null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<ModuloResponseDTO> result = moduloService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Modulo Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(moduloRepository).findAllByFilter(null, "Modulo Test", null, null);
    }

    @Test
    @DisplayName("Should list all modulos when filter is null")
    void listAllByFilterCase3() {
        // Mock para simular resposta do Repository
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        ModuloEntity entidade1 = ModuloEntity.builder().nome("Modulo Test 1").rota("/modulo1").sessao(sessao).build();
        ModuloEntity entidade2 = ModuloEntity.builder().nome("Modulo Test 2").rota("/modulo2").sessao(sessao).build();
        Page<ModuloEntity> paginaMock = new PageImpl<>(List.of(entidade1, entidade2));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(moduloRepository.findAllByFilter(null, null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter passando filtro nulo
        Page<ModuloResponseDTO> result = moduloService.listAllByFilter(null, null);

        assertThat(result.getContent()).hasSize(2);

        // verifica se ao chamar a findAllByFilter ele passou todos os parametros nulos
        verify(moduloRepository).findAllByFilter(null, null, null, null);
    }

    @Test
    @DisplayName("Should not list modulo when filter does not match")
    void listAllByFilterCase4() {
        // inicializa o filtro
        ModuloFilterDTO filtro = ModuloFilterDTO.builder().nome("Modulo Inexistente").rota("/naoExiste").build();

        // Mock para simular resposta do Repository
        Page<ModuloEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(moduloRepository.findAllByFilter(null, "Modulo Inexistente", "/naoExiste", null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<ModuloResponseDTO> result = moduloService.listAllByFilter(null, filtro);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(moduloRepository).findAllByFilter(null, "Modulo Inexistente", "/naoExiste", null);
    }

    @Test
    @DisplayName("Should return modulo when id exists")
    void findCase1() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Repository
        SessaoEntity sessao = SessaoEntity.builder().nome("Sessao").rota("/rotaSessao").build();
        ModuloEntity entidade = ModuloEntity.builder().id(id).nome("Modulo Test").rota("/modulo").sessao(sessao).build();

        // Quando chamar findById ele retorna o mock entidade
        when(moduloRepository.findById(id)).thenReturn(Optional.of(entidade));

        // chama a funcao find
        ModuloResponseDTO result = moduloService.find(null, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Modulo Test");
        assertThat(result.getRota()).isEqualTo("/modulo");

        // verifica se ao chamar a findById ele passou o mesmo id
        verify(moduloRepository).findById(id);
    }

    @Test
    @DisplayName("Should throw exception when modulo id does not exist")
    void findCase2() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar findById ele retorna vazio
        when(moduloRepository.findById(id)).thenReturn(Optional.empty());

        // chama a funcao find e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> moduloService.find(null, id));

        // verifica se ao chamar a findById ele passou o mesmo id
        verify(moduloRepository).findById(id);
    }

    @Test
    @DisplayName("Should save and return created modulo")
    void createCase1() {
        // id da sessao utilizada na criacao
        UUID idSessao = UUID.randomUUID();
        SessaoEntity sessao = SessaoEntity.builder().id(idSessao).nome("Sessao").rota("/rotaSessao").build();

        // dados para criacao
        ModuloCreateDTO modulo = ModuloCreateDTO.builder().idSessao(idSessao).nome("Modulo Test").rota("/modulo").build();

        // Mock para simular que a sessao existe
        when(sessaoRepository.findById(idSessao)).thenReturn(Optional.of(sessao));

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        ModuloEntity entidadeSalva = ModuloEntity.builder().id(id).nome("Modulo Test").rota("/modulo").sessao(sessao).build();
        when(moduloRepository.save(any(ModuloEntity.class))).thenReturn(entidadeSalva);

        // chama a funcao create
        ModuloResponseDTO result = moduloService.create(modulo);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Modulo Test");
        assertThat(result.getRota()).isEqualTo("/modulo");
        assertThat(result.getSessao()).isEqualTo(sessao);

        // verifica se buscou a sessao antes de criar
        verify(sessaoRepository).findById(idSessao);

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(moduloRepository).save(argThat(e -> e.getNome().equals("Modulo Test") && e.getRota().equals("/modulo") && e.getSessao().equals(sessao)));
    }

    @Test
    @DisplayName("Should throw exception when creating modulo with a sessao that does not exist")
    void createCase2() {
        // id da sessao inexistente
        UUID idSessao = UUID.randomUUID();

        // dados para criacao
        ModuloCreateDTO modulo = ModuloCreateDTO.builder().idSessao(idSessao).nome("Modulo Test").rota("/modulo").build();

        // Mock para simular que a sessao nao existe
        when(sessaoRepository.findById(idSessao)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> moduloService.create(modulo));

        // verifica se buscou a sessao antes de tentar criar
        verify(sessaoRepository).findById(idSessao);

        // verifica se nunca chegou a salvar, ja que a sessao nao existe
        verify(moduloRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update modulo when id and sessao exist")
    void updateCase1() {
        // id do modulo e da sessao utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idSessao = UUID.randomUUID();

        // dados para atualizacao
        ModuloCreateDTO modulo = ModuloCreateDTO.builder().idSessao(idSessao).nome("Modulo Test").rota("/modulo").build();

        // Mock para simular que o modulo existe
        SessaoEntity sessaoAntiga = SessaoEntity.builder().nome("Sessao Old").rota("/old").build();
        ModuloEntity entidadeExistente = ModuloEntity.builder().id(id).nome("Modulo Old").rota("/old").sessao(sessaoAntiga).build();
        when(moduloRepository.findById(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que a sessao existe
        SessaoEntity sessao = SessaoEntity.builder().id(idSessao).nome("Sessao").rota("/rotaSessao").build();
        when(sessaoRepository.findById(idSessao)).thenReturn(Optional.of(sessao));

        // Mock para simular resposta do save
        ModuloEntity entidadeAtualizada = ModuloEntity.builder().id(id).nome("Modulo Test").rota("/modulo").sessao(sessao).build();
        when(moduloRepository.save(any(ModuloEntity.class))).thenReturn(entidadeAtualizada);

        // chama a funcao update
        ModuloResponseDTO result = moduloService.update(modulo, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Modulo Test");
        assertThat(result.getRota()).isEqualTo("/modulo");
        assertThat(result.getSessao()).isEqualTo(sessao);

        // verifica se buscou o modulo e a sessao antes de atualizar
        verify(moduloRepository).findById(id);
        verify(sessaoRepository).findById(idSessao);

        // verifica se salvou a entidade com os dados atualizados
        verify(moduloRepository).save(argThat(e -> e.getId().equals(id) && e.getNome().equals("Modulo Test") && e.getRota().equals("/modulo") && e.getSessao().equals(sessao)));
    }

    @Test
    @DisplayName("Should throw exception when updating a modulo that does not exist")
    void updateCase2() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        ModuloCreateDTO modulo = ModuloCreateDTO.builder().idSessao(UUID.randomUUID()).nome("Modulo Test").rota("/modulo").build();

        // Mock para simular que o modulo nao existe
        when(moduloRepository.findById(id)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> moduloService.update(modulo, id));

        // verifica se buscou o modulo antes de tentar atualizar
        verify(moduloRepository).findById(id);

        // verifica se nunca chegou a buscar a sessao nem a salvar
        verify(sessaoRepository, never()).findById(any(UUID.class));
        verify(moduloRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating modulo with a sessao that does not exist")
    void updateCase3() {
        // id do modulo e da sessao utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idSessao = UUID.randomUUID();
        ModuloCreateDTO modulo = ModuloCreateDTO.builder().idSessao(idSessao).nome("Modulo Test").rota("/modulo").build();

        // Mock para simular que o modulo existe
        SessaoEntity sessaoAntiga = SessaoEntity.builder().nome("Sessao Old").rota("/old").build();
        ModuloEntity entidadeExistente = ModuloEntity.builder().id(id).nome("Modulo Old").rota("/old").sessao(sessaoAntiga).build();
        when(moduloRepository.findById(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que a sessao nao existe
        when(sessaoRepository.findById(idSessao)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> moduloService.update(modulo, id));

        // verifica se buscou o modulo e a sessao antes de tentar atualizar
        verify(moduloRepository).findById(id);
        verify(sessaoRepository).findById(idSessao);

        // verifica se nunca chegou a salvar, ja que a sessao nao existe
        verify(moduloRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete modulo by id")
    void deleteCase1() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // chama a funcao delete
        moduloService.delete(id);

        // verifica se excluiu o id correto
        verify(moduloRepository).deleteById(id);
    }
}