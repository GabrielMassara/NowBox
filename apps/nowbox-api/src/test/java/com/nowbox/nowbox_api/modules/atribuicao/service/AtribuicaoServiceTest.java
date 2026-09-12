package com.nowbox.nowbox_api.modules.atribuicao.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.atribuicao.dto.AtribuicaoCreateDTO;
import com.nowbox.nowbox_api.modules.atribuicao.dto.AtribuicaoFilterDTO;
import com.nowbox.nowbox_api.modules.atribuicao.dto.AtribuicaoResponseDTO;
import com.nowbox.nowbox_api.modules.atribuicao.entity.AtribuicaoEntity;
import com.nowbox.nowbox_api.modules.atribuicao.repository.IAtribuicaoRepository;
import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.cargo.repository.ICargoRepository;
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
class AtribuicaoServiceTest {

    @Mock
    private IAtribuicaoRepository atribuicaoRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private ICargoRepository cargoRepository;

    @InjectMocks
    private AtribuicaoService atribuicaoService;

    @Test
    @DisplayName("Should list one atribuicao filtered by idUsuario and idCargo")
    void listAllByFilterCase1() {
        // ids utilizados no filtro
        UUID idUsuario = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();
        UsuarioEntity usuario = UsuarioEntity.builder().id(idUsuario).nome("Usuario").build();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();

        // inicializa o filtro
        AtribuicaoFilterDTO filtro = AtribuicaoFilterDTO.builder().idUsuario(idUsuario).idCargo(idCargo).build();

        // Mock para simular resposta do Repository
        AtribuicaoEntity entidade = AtribuicaoEntity.builder().usuario(usuario).cargo(cargo).build();
        Page<AtribuicaoEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(atribuicaoRepository.findAllByFilter(idUsuario, idCargo, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<AtribuicaoResponseDTO> result = atribuicaoService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getUsuario()).isEqualTo(usuario);
        assertThat(result.getContent().getFirst().getCargo()).isEqualTo(cargo);

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(atribuicaoRepository).findAllByFilter(idUsuario, idCargo, null);
    }

    @Test
    @DisplayName("Should list one atribuicao filtered by idUsuario")
    void listAllByFilterCase2() {
        // inicializa o filtro
        UUID idUsuario = UUID.randomUUID();
        AtribuicaoFilterDTO filtro = AtribuicaoFilterDTO.builder().idUsuario(idUsuario).build();

        // Mock para simular resposta do Repository
        UsuarioEntity usuario = UsuarioEntity.builder().id(idUsuario).nome("Usuario").build();
        CargoEntity cargo = CargoEntity.builder().nome("Cargo").build();
        AtribuicaoEntity entidade = AtribuicaoEntity.builder().usuario(usuario).cargo(cargo).build();
        Page<AtribuicaoEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(atribuicaoRepository.findAllByFilter(idUsuario, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<AtribuicaoResponseDTO> result = atribuicaoService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getUsuario()).isEqualTo(usuario);

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(atribuicaoRepository).findAllByFilter(idUsuario, null, null);
    }

    @Test
    @DisplayName("Should list all atribuicoes when filter is null")
    void listAllByFilterCase3() {
        // Mock para simular resposta do Repository
        UsuarioEntity usuario = UsuarioEntity.builder().nome("Usuario").build();
        CargoEntity cargo = CargoEntity.builder().nome("Cargo").build();
        AtribuicaoEntity entidade1 = AtribuicaoEntity.builder().usuario(usuario).cargo(cargo).build();
        AtribuicaoEntity entidade2 = AtribuicaoEntity.builder().usuario(usuario).cargo(cargo).build();
        Page<AtribuicaoEntity> paginaMock = new PageImpl<>(List.of(entidade1, entidade2));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(atribuicaoRepository.findAllByFilter(null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter passando filtro nulo
        Page<AtribuicaoResponseDTO> result = atribuicaoService.listAllByFilter(null, null);

        assertThat(result.getContent()).hasSize(2);

        // verifica se ao chamar a findAllByFilter ele passou todos os parametros nulos
        verify(atribuicaoRepository).findAllByFilter(null, null, null);
    }

    @Test
    @DisplayName("Should not list atribuicao when filter does not match")
    void listAllByFilterCase4() {
        // inicializa o filtro
        UUID idUsuario = UUID.randomUUID();
        AtribuicaoFilterDTO filtro = AtribuicaoFilterDTO.builder().idUsuario(idUsuario).build();

        // Mock para simular resposta do Repository
        Page<AtribuicaoEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(atribuicaoRepository.findAllByFilter(idUsuario, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<AtribuicaoResponseDTO> result = atribuicaoService.listAllByFilter(null, filtro);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(atribuicaoRepository).findAllByFilter(idUsuario, null, null);
    }

    @Test
    @DisplayName("Should return atribuicao when id exists")
    void findCase1() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Repository
        UsuarioEntity usuario = UsuarioEntity.builder().nome("Usuario").build();
        CargoEntity cargo = CargoEntity.builder().nome("Cargo").build();
        AtribuicaoEntity entidade = AtribuicaoEntity.builder().id(id).usuario(usuario).cargo(cargo).build();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna o mock entidade
        when(atribuicaoRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidade));

        // chama a funcao find
        AtribuicaoResponseDTO result = atribuicaoService.find(null, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUsuario()).isEqualTo(usuario);
        assertThat(result.getCargo()).isEqualTo(cargo);

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(atribuicaoRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should throw exception when atribuicao id does not exist")
    void findCase2() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna vazio
        when(atribuicaoRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao find e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> atribuicaoService.find(null, id));

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(atribuicaoRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should save and return created atribuicao")
    void createCase1() {
        // ids do usuario e do cargo utilizados na criacao
        UUID idUsuario = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();
        UsuarioEntity usuario = UsuarioEntity.builder().id(idUsuario).nome("Usuario").build();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();

        // dados para criacao
        AtribuicaoCreateDTO atribuicao = AtribuicaoCreateDTO.builder().idUsuario(idUsuario).idCargo(idCargo).build();

        // Mock para simular que o usuario e o cargo existem
        when(usuarioRepository.findByIdAndDeletedAtIsNull(idUsuario)).thenReturn(Optional.of(usuario));
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.of(cargo));

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        AtribuicaoEntity entidadeSalva = AtribuicaoEntity.builder().id(id).usuario(usuario).cargo(cargo).createdAt(LocalDateTime.now()).build();
        when(atribuicaoRepository.save(any(AtribuicaoEntity.class))).thenReturn(entidadeSalva);

        // chama a funcao create
        AtribuicaoResponseDTO result = atribuicaoService.create(atribuicao);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUsuario()).isEqualTo(usuario);
        assertThat(result.getCargo()).isEqualTo(cargo);

        // verifica se buscou o usuario e o cargo antes de criar
        verify(usuarioRepository).findByIdAndDeletedAtIsNull(idUsuario);
        verify(cargoRepository).findById(idCargo);

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(atribuicaoRepository).save(argThat(e -> e.getUsuario().equals(usuario) && e.getCargo().equals(cargo)));
    }

    @Test
    @DisplayName("Should throw exception when creating atribuicao with a usuario that does not exist")
    void createCase2() {
        // id do usuario inexistente
        UUID idUsuario = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();

        // dados para criacao
        AtribuicaoCreateDTO atribuicao = AtribuicaoCreateDTO.builder().idUsuario(idUsuario).idCargo(idCargo).build();

        // Mock para simular que o usuario nao existe
        when(usuarioRepository.findByIdAndDeletedAtIsNull(idUsuario)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> atribuicaoService.create(atribuicao));

        // verifica se buscou o usuario antes de tentar criar
        verify(usuarioRepository).findByIdAndDeletedAtIsNull(idUsuario);

        // verifica se nunca chegou a buscar o cargo nem a salvar
        verify(cargoRepository, never()).findById(any(UUID.class));
        verify(atribuicaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating atribuicao with a cargo that does not exist")
    void createCase3() {
        // ids do usuario e do cargo inexistente
        UUID idUsuario = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();
        UsuarioEntity usuario = UsuarioEntity.builder().id(idUsuario).nome("Usuario").build();

        // dados para criacao
        AtribuicaoCreateDTO atribuicao = AtribuicaoCreateDTO.builder().idUsuario(idUsuario).idCargo(idCargo).build();

        // Mock para simular que o usuario existe, mas o cargo nao
        when(usuarioRepository.findByIdAndDeletedAtIsNull(idUsuario)).thenReturn(Optional.of(usuario));
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> atribuicaoService.create(atribuicao));

        // verifica se buscou o usuario e o cargo antes de tentar criar
        verify(usuarioRepository).findByIdAndDeletedAtIsNull(idUsuario);
        verify(cargoRepository).findById(idCargo);

        // verifica se nunca chegou a salvar, ja que o cargo nao existe
        verify(atribuicaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update atribuicao when id, usuario and cargo exist")
    void updateCase1() {
        // ids da atribuicao, do usuario e do cargo utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idUsuario = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();

        // dados para atualizacao
        AtribuicaoCreateDTO atribuicao = AtribuicaoCreateDTO.builder().idUsuario(idUsuario).idCargo(idCargo).build();

        // Mock para simular que a atribuicao existe
        UsuarioEntity usuarioAntigo = UsuarioEntity.builder().nome("Usuario Old").build();
        CargoEntity cargoAntigo = CargoEntity.builder().nome("Cargo Old").build();
        AtribuicaoEntity entidadeExistente = AtribuicaoEntity.builder().id(id).usuario(usuarioAntigo).cargo(cargoAntigo).createdAt(LocalDateTime.now()).build();
        when(atribuicaoRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o usuario e o cargo existem
        UsuarioEntity usuario = UsuarioEntity.builder().id(idUsuario).nome("Usuario").build();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        when(usuarioRepository.findByIdAndDeletedAtIsNull(idUsuario)).thenReturn(Optional.of(usuario));
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.of(cargo));

        // Mock para simular resposta do save
        AtribuicaoEntity entidadeAtualizada = AtribuicaoEntity.builder().id(id).usuario(usuario).cargo(cargo).build();
        when(atribuicaoRepository.save(any(AtribuicaoEntity.class))).thenReturn(entidadeAtualizada);

        // chama a funcao update
        AtribuicaoResponseDTO result = atribuicaoService.update(atribuicao, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUsuario()).isEqualTo(usuario);
        assertThat(result.getCargo()).isEqualTo(cargo);

        // verifica se buscou a atribuicao, o usuario e o cargo antes de atualizar
        verify(atribuicaoRepository).findByIdAndDeletedAtIsNull(id);
        verify(usuarioRepository).findByIdAndDeletedAtIsNull(idUsuario);
        verify(cargoRepository).findById(idCargo);

        // verifica se salvou a entidade com os dados atualizados
        verify(atribuicaoRepository).save(argThat(e -> e.getId().equals(id) && e.getUsuario().equals(usuario) && e.getCargo().equals(cargo)));
    }

    @Test
    @DisplayName("Should throw exception when updating an atribuicao that does not exist")
    void updateCase2() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        AtribuicaoCreateDTO atribuicao = AtribuicaoCreateDTO.builder().idUsuario(UUID.randomUUID()).idCargo(UUID.randomUUID()).build();

        // Mock para simular que a atribuicao nao existe
        when(atribuicaoRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> atribuicaoService.update(atribuicao, id));

        // verifica se buscou a atribuicao antes de tentar atualizar
        verify(atribuicaoRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se nunca chegou a buscar o usuario, o cargo nem a salvar
        verify(usuarioRepository, never()).findByIdAndDeletedAtIsNull(any(UUID.class));
        verify(cargoRepository, never()).findById(any(UUID.class));
        verify(atribuicaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating atribuicao with a usuario that does not exist")
    void updateCase3() {
        // ids da atribuicao e do usuario utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idUsuario = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();
        AtribuicaoCreateDTO atribuicao = AtribuicaoCreateDTO.builder().idUsuario(idUsuario).idCargo(idCargo).build();

        // Mock para simular que a atribuicao existe
        UsuarioEntity usuarioAntigo = UsuarioEntity.builder().nome("Usuario Old").build();
        CargoEntity cargoAntigo = CargoEntity.builder().nome("Cargo Old").build();
        AtribuicaoEntity entidadeExistente = AtribuicaoEntity.builder().id(id).usuario(usuarioAntigo).cargo(cargoAntigo).build();
        when(atribuicaoRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o usuario nao existe
        when(usuarioRepository.findByIdAndDeletedAtIsNull(idUsuario)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> atribuicaoService.update(atribuicao, id));

        // verifica se buscou a atribuicao e o usuario antes de tentar atualizar
        verify(atribuicaoRepository).findByIdAndDeletedAtIsNull(id);
        verify(usuarioRepository).findByIdAndDeletedAtIsNull(idUsuario);

        // verifica se nunca chegou a buscar o cargo nem a salvar, ja que o usuario nao existe
        verify(cargoRepository, never()).findById(any(UUID.class));
        verify(atribuicaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating atribuicao with a cargo that does not exist")
    void updateCase4() {
        // ids da atribuicao, do usuario e do cargo utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idUsuario = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();
        AtribuicaoCreateDTO atribuicao = AtribuicaoCreateDTO.builder().idUsuario(idUsuario).idCargo(idCargo).build();

        // Mock para simular que a atribuicao existe
        UsuarioEntity usuarioAntigo = UsuarioEntity.builder().nome("Usuario Old").build();
        CargoEntity cargoAntigo = CargoEntity.builder().nome("Cargo Old").build();
        AtribuicaoEntity entidadeExistente = AtribuicaoEntity.builder().id(id).usuario(usuarioAntigo).cargo(cargoAntigo).build();
        when(atribuicaoRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o usuario existe, mas o cargo nao
        UsuarioEntity usuario = UsuarioEntity.builder().id(idUsuario).nome("Usuario").build();
        when(usuarioRepository.findByIdAndDeletedAtIsNull(idUsuario)).thenReturn(Optional.of(usuario));
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> atribuicaoService.update(atribuicao, id));

        // verifica se buscou a atribuicao, o usuario e o cargo antes de tentar atualizar
        verify(atribuicaoRepository).findByIdAndDeletedAtIsNull(id);
        verify(usuarioRepository).findByIdAndDeletedAtIsNull(idUsuario);
        verify(cargoRepository).findById(idCargo);

        // verifica se nunca chegou a salvar, ja que o cargo nao existe
        verify(atribuicaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete atribuicao by id")
    void deleteCase1() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que a atribuicao existe
        AtribuicaoEntity entidadeExistente = AtribuicaoEntity.builder().id(id).build();
        when(atribuicaoRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // chama a funcao delete
        atribuicaoService.delete(id);

        // verifica se buscou a atribuicao antes de excluir
        verify(atribuicaoRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se salvou a entidade com o deletedAt preenchido
        verify(atribuicaoRepository).save(argThat(e -> e.getId().equals(id) && e.getDeletedAt() != null));
    }

    @Test
    @DisplayName("Should throw exception when deleting an atribuicao that does not exist")
    void deleteCase2() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que a atribuicao nao existe
        when(atribuicaoRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao delete e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> atribuicaoService.delete(id));

        // verifica se nunca chegou a salvar, ja que a atribuicao nao existe
        verify(atribuicaoRepository, never()).save(any());
    }
}
