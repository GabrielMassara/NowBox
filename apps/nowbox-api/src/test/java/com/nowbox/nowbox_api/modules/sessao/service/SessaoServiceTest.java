package com.nowbox.nowbox_api.modules.sessao.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.sessao.dto.SessaoCreateDTO;
import com.nowbox.nowbox_api.modules.sessao.dto.SessaoRequestFilterDTO;
import com.nowbox.nowbox_api.modules.sessao.dto.SessaoResponseDTO;
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
class SessaoServiceTest {

    @Mock
    private ISessaoRepository sessaoRepository;

    @InjectMocks
    private SessaoService sessaoService;

    @Test
    @DisplayName("Should list one session filtered by rota and nome")
    void listAllCase1() {
        // inicializa o filtro
        SessaoRequestFilterDTO filter = SessaoRequestFilterDTO.builder().nome("Session Test").rota("/test").build();

        // Mock para simular resposta do Repository
        SessaoEntity entidade = SessaoEntity.builder().nome("Session Test").rota("/test").build();
        Page<SessaoEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFiltro ele retorna o mock paginaMock
        when(sessaoRepository.findAllByFiltro("Session Test", "/test", null)).thenReturn(paginaMock);

        // chama a funcal listAll
        Page<SessaoResponseDTO> result = sessaoService.listAll(null, filter);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Session Test");

        // verifica se ao chamar a findAllByFiltro ele passou os mesmos parametros
        verify(sessaoRepository).findAllByFiltro("Session Test", "/test", null);
    }

    @Test
    @DisplayName("Should list one session filtered by nome")
    void listAllCase2() {
        // inicializa o filtro
        SessaoRequestFilterDTO filter = SessaoRequestFilterDTO.builder().nome("Session Test").rota(null).build();

        // Mock para simular resposta do Repository
        SessaoEntity entidade = SessaoEntity.builder().nome("Session Test").rota("/test").build();
        Page<SessaoEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFiltro ele retorna o mock paginaMock
        when(sessaoRepository.findAllByFiltro("Session Test", null, null)).thenReturn(paginaMock);

        // chama a funcal listAll
        Page<SessaoResponseDTO> result = sessaoService.listAll(null, filter);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Session Test");

        // verifica se ao chamar a findAllByFiltro ele passou os mesmos parametros
        verify(sessaoRepository).findAllByFiltro("Session Test", null, null);
    }

    @Test
    @DisplayName("Should list one session filtered by rota")
    void listAllCase3() {
        // inicializa o filtro
        SessaoRequestFilterDTO filter = SessaoRequestFilterDTO.builder().nome(null).rota("/test").build();

        // Mock para simular resposta do Repository
        SessaoEntity entidade = SessaoEntity.builder().nome("Session Test").rota("/test").build();
        Page<SessaoEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFiltro ele retorna o mock paginaMock
        when(sessaoRepository.findAllByFiltro(null, "/test", null)).thenReturn(paginaMock);

        // chama a funcal listAll
        Page<SessaoResponseDTO> result = sessaoService.listAll(null, filter);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Session Test");

        // verifica se ao chamar a findAllByFiltro ele passou os mesmos parametros
        verify(sessaoRepository).findAllByFiltro(null, "/test", null);
    }

    @Test
    @DisplayName("Should not list session (both parameters wrong)")
    void listAllCase4() {
        // inicializa o filtro
        SessaoRequestFilterDTO filter = SessaoRequestFilterDTO.builder().nome("Session Test wrong").rota("/testWrong").build();

        // Mock para simular resposta do Repository
        Page<SessaoEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFiltro ele retorna o mock paginaMock
        when(sessaoRepository.findAllByFiltro("Session Test wrong", "/testWrong", null)).thenReturn(paginaMock);

        // chama a funcal listAll
        Page<SessaoResponseDTO> result = sessaoService.listAll(null, filter);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFiltro ele passou os mesmos parametros
        verify(sessaoRepository).findAllByFiltro("Session Test wrong", "/testWrong", null);
    }

    @Test
    @DisplayName("Should return session when id exists")
    void listByIdCase1() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Repository
        SessaoEntity entidade = SessaoEntity.builder().id(id).nome("Session Test").rota("/test").build();

        // Quando chamar findById ele retorna o mock entidade
        when(sessaoRepository.findById(id)).thenReturn(Optional.of(entidade));

        // chama a funcao listById
        Optional<SessaoResponseDTO> result = sessaoService.listById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getNome()).isEqualTo("Session Test");

        // verifica se ao chamar a findById ele passou o mesmo id
        verify(sessaoRepository).findById(id);
    }

    @Test
    @DisplayName("Should return empty when id does not exist")
    void listByIdCase2() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar findById ele retorna vazio
        when(sessaoRepository.findById(id)).thenReturn(Optional.empty());

        // chama a funcao listById
        Optional<SessaoResponseDTO> result = sessaoService.listById(id);

        assertThat(result).isEmpty();

        // verifica se ao chamar a findById ele passou o mesmo id
        verify(sessaoRepository).findById(id);
    }

    @Test
    @DisplayName("Should save and return created session")
    void saveCase1() {
        // dados para criacao
        SessaoCreateDTO sessao = SessaoCreateDTO.builder().nome("Session Test").rota("/test").build();

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        SessaoEntity entidadeSalva = SessaoEntity.builder().id(id).nome("Session Test").rota("/test").build();

        // Quando chamar save ele retorna a entidade salva
        when(sessaoRepository.save(any(SessaoEntity.class))).thenReturn(entidadeSalva);

        // chama a funcao save
        SessaoResponseDTO result = sessaoService.save(sessao);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Session Test");
        assertThat(result.getRota()).isEqualTo("/test");

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(sessaoRepository).save(argThat(e -> e.getNome().equals("Session Test") && e.getRota().equals("/test")));
    }

    @Test
    @DisplayName("Should update session when id exists")
    void updateCase1() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        SessaoCreateDTO sessao = SessaoCreateDTO.builder().nome("Session Test").rota("/test").build();

        // Mock para simular que a sessao existe
        SessaoEntity entidadeExistente = SessaoEntity.builder().id(id).nome("Session Old").rota("/old").build();
        when(sessaoRepository.findById(id)).thenReturn(Optional.of(entidadeExistente));

        // chama a funcao update
        sessaoService.update(id, sessao);

        // verifica se buscou a sessao antes de atualizar
        verify(sessaoRepository).findById(id);

        // verifica se salvou a entidade com os dados atualizados
        verify(sessaoRepository).save(argThat(e -> e.getId().equals(id) && e.getNome().equals("Session Test") && e.getRota().equals("/test")));
    }

    @Test
    @DisplayName("Should throw exception when updating a session that does not exist")
    void updateCase2() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        SessaoCreateDTO sessao = SessaoCreateDTO.builder().nome("Session Test").rota("/test").build();

        // Mock para simular que a sessao nao existe
        when(sessaoRepository.findById(id)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> sessaoService.update(id, sessao));

        // verifica se buscou a sessao antes de tentar atualizar
        verify(sessaoRepository).findById(id);

        // verifica se nunca chegou a salvar, ja que a sessao nao existe
        verify(sessaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete session when id exists")
    void deleteCase1() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que a sessao existe
        SessaoEntity entidadeExistente = SessaoEntity.builder().id(id).nome("Session Test").rota("/test").build();
        when(sessaoRepository.findById(id)).thenReturn(Optional.of(entidadeExistente));

        // chama a funcao delete
        sessaoService.delete(id);

        // verifica se buscou a sessao antes de excluir
        verify(sessaoRepository).findById(id);

        // verifica se excluiu o id correto
        verify(sessaoRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should throw exception when deleting a session that does not exist")
    void deleteCase2() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que a sessao nao existe
        when(sessaoRepository.findById(id)).thenReturn(Optional.empty());

        // chama a funcao delete e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> sessaoService.delete(id));

        // verifica se buscou a sessao antes de tentar excluir
        verify(sessaoRepository).findById(id);

        // verifica se nunca chegou a excluir, ja que a sessao nao existe
        verify(sessaoRepository, never()).deleteById(any(UUID.class));
    }
}