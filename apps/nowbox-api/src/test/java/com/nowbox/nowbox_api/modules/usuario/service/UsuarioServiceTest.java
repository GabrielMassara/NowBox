package com.nowbox.nowbox_api.modules.usuario.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.usuario.dto.UsuarioCreateDTO;
import com.nowbox.nowbox_api.modules.usuario.dto.UsuarioFilterDTO;
import com.nowbox.nowbox_api.modules.usuario.dto.UsuarioResponseDTO;
import com.nowbox.nowbox_api.modules.usuario.entity.UsuarioEntity;
import com.nowbox.nowbox_api.modules.usuario.repository.IUsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
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
class UsuarioServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Should list one usuario filtered by nome, email and cpf")
    void listAllByFilterCase1() {
        // inicializa o filtro
        UsuarioFilterDTO filtro = UsuarioFilterDTO.builder().nome("Usuario Test").email("usuario@test.com").cpf("12345678901").build();

        // Mock para simular resposta do Repository
        UsuarioEntity entidade = UsuarioEntity.builder().nome("Usuario Test").email("usuario@test.com").cpf("12345678901").build();
        Page<UsuarioEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(usuarioRepository.findAllByFilter("Usuario Test", "usuario@test.com", "12345678901", null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<UsuarioResponseDTO> result = usuarioService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Usuario Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(usuarioRepository).findAllByFilter("Usuario Test", "usuario@test.com", "12345678901", null);
    }

    @Test
    @DisplayName("Should list one usuario filtered by nome")
    void listAllByFilterCase2() {
        // inicializa o filtro
        UsuarioFilterDTO filtro = UsuarioFilterDTO.builder().nome("Usuario Test").build();

        // Mock para simular resposta do Repository
        UsuarioEntity entidade = UsuarioEntity.builder().nome("Usuario Test").build();
        Page<UsuarioEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(usuarioRepository.findAllByFilter("Usuario Test", null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<UsuarioResponseDTO> result = usuarioService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Usuario Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(usuarioRepository).findAllByFilter("Usuario Test", null, null, null);
    }

    @Test
    @DisplayName("Should list all usuarios when filter is null")
    void listAllByFilterCase3() {
        // Mock para simular resposta do Repository
        UsuarioEntity entidade1 = UsuarioEntity.builder().nome("Usuario Test 1").build();
        UsuarioEntity entidade2 = UsuarioEntity.builder().nome("Usuario Test 2").build();
        Page<UsuarioEntity> paginaMock = new PageImpl<>(List.of(entidade1, entidade2));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(usuarioRepository.findAllByFilter(null, null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter passando filtro nulo
        Page<UsuarioResponseDTO> result = usuarioService.listAllByFilter(null, null);

        assertThat(result.getContent()).hasSize(2);

        // verifica se ao chamar a findAllByFilter ele passou todos os parametros nulos
        verify(usuarioRepository).findAllByFilter(null, null, null, null);
    }

    @Test
    @DisplayName("Should not list usuario when filter does not match")
    void listAllByFilterCase4() {
        // inicializa o filtro
        UsuarioFilterDTO filtro = UsuarioFilterDTO.builder().nome("Usuario Inexistente").build();

        // Mock para simular resposta do Repository
        Page<UsuarioEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(usuarioRepository.findAllByFilter("Usuario Inexistente", null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<UsuarioResponseDTO> result = usuarioService.listAllByFilter(null, filtro);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(usuarioRepository).findAllByFilter("Usuario Inexistente", null, null, null);
    }

    @Test
    @DisplayName("Should return usuario when id exists")
    void findCase1() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Repository
        UsuarioEntity entidade = UsuarioEntity.builder().id(id).nome("Usuario Test").email("usuario@test.com").build();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna o mock entidade
        when(usuarioRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidade));

        // chama a funcao find
        UsuarioResponseDTO result = usuarioService.find(null, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Usuario Test");
        assertThat(result.getEmail()).isEqualTo("usuario@test.com");

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(usuarioRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should throw exception when usuario id does not exist")
    void findCase2() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna vazio
        when(usuarioRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao find e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> usuarioService.find(null, id));

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(usuarioRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should save and return created usuario")
    void createCase1() {
        // dados para criacao
        UsuarioCreateDTO usuario = UsuarioCreateDTO.builder()
                .nome("Usuario Test")
                .email("usuario@test.com")
                .cpf("12345678901")
                .sexo("M")
                .senha("senha123")
                .build();

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        UsuarioEntity entidadeSalva = UsuarioEntity.builder()
                .id(id)
                .nome("Usuario Test")
                .email("usuario@test.com")
                .cpf("12345678901")
                .sexo("M")
                .senha("senha123")
                .createdAt(LocalDateTime.now())
                .build();
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenReturn(entidadeSalva);

        // chama a funcao create
        UsuarioResponseDTO result = usuarioService.create(usuario);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Usuario Test");
        assertThat(result.getEmail()).isEqualTo("usuario@test.com");
        assertThat(result.getCpf()).isEqualTo("12345678901");

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(usuarioRepository).save(argThat(e -> e.getNome().equals("Usuario Test") && e.getEmail().equals("usuario@test.com") && e.getCpf().equals("12345678901") && e.getSenha().equals("senha123")));
    }

    @Test
    @DisplayName("Should update usuario when id exists")
    void updateCase1() {
        // id da usuario utilizado na atualizacao
        UUID id = UUID.randomUUID();

        // dados para atualizacao
        UsuarioCreateDTO usuario = UsuarioCreateDTO.builder().nome("Usuario Test").email("usuario@test.com").cpf("12345678901").sexo("M").senha("novaSenha").build();

        // Mock para simular que o usuario existe
        UsuarioEntity entidadeExistente = UsuarioEntity.builder().id(id).nome("Usuario Old").email("old@test.com").createdAt(LocalDateTime.now()).build();
        when(usuarioRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular resposta do save
        UsuarioEntity entidadeAtualizada = UsuarioEntity.builder().id(id).nome("Usuario Test").email("usuario@test.com").cpf("12345678901").sexo("M").build();
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenReturn(entidadeAtualizada);

        // chama a funcao update
        UsuarioResponseDTO result = usuarioService.update(usuario, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Usuario Test");
        assertThat(result.getEmail()).isEqualTo("usuario@test.com");

        // verifica se buscou o usuario antes de atualizar
        verify(usuarioRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se salvou a entidade com os dados atualizados
        verify(usuarioRepository).save(argThat(e -> e.getId().equals(id) && e.getNome().equals("Usuario Test") && e.getEmail().equals("usuario@test.com")));
    }

    @Test
    @DisplayName("Should throw exception when updating a usuario that does not exist")
    void updateCase2() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UsuarioCreateDTO usuario = UsuarioCreateDTO.builder().nome("Usuario Test").build();

        // Mock para simular que o usuario nao existe
        when(usuarioRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> usuarioService.update(usuario, id));

        // verifica se buscou o usuario antes de tentar atualizar
        verify(usuarioRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se nunca chegou a salvar, ja que o usuario nao existe
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete usuario by id")
    void deleteCase1() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o usuario existe
        UsuarioEntity entidadeExistente = UsuarioEntity.builder().id(id).nome("Usuario Test").build();
        when(usuarioRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // chama a funcao delete
        usuarioService.delete(id);

        // verifica se buscou o usuario antes de excluir
        verify(usuarioRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se salvou a entidade com o deletedAt preenchido
        verify(usuarioRepository).save(argThat(e -> e.getId().equals(id) && e.getDeletedAt() != null));
    }

    @Test
    @DisplayName("Should throw exception when deleting a usuario that does not exist")
    void deleteCase2() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o usuario nao existe
        when(usuarioRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao delete e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> usuarioService.delete(id));

        // verifica se nunca chegou a salvar, ja que o usuario nao existe
        verify(usuarioRepository, never()).save(any());
    }
}
