package com.nowbox.nowbox_api.modules.permissao.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.cargo.repository.ICargoRepository;
import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import com.nowbox.nowbox_api.modules.operacao.repository.IOperacaoRepository;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoCreateDTO;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoFilterDTO;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoResponseDTO;
import com.nowbox.nowbox_api.modules.permissao.entity.PermissaoEntity;
import com.nowbox.nowbox_api.modules.permissao.repository.IPermissaoRepository;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
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
class PermissaoServiceTest {

    @Mock
    private IPermissaoRepository permissaoRepository;

    @Mock
    private ICargoRepository cargoRepository;

    @Mock
    private IOperacaoRepository operacaoRepository;

    @InjectMocks
    private PermissaoService permissaoService;

    @Test
    @DisplayName("Should list one permissao filtered by idCargo and idOperacao")
    void listAllByFilterCase1() {
        // ids utilizados no filtro
        UUID idCargo = UUID.randomUUID();
        UUID idOperacao = UUID.randomUUID();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().id(idOperacao).nome("Operacao").codigo("OP1").build();

        // inicializa o filtro
        PermissaoFilterDTO filtro = PermissaoFilterDTO.builder().idCargo(idCargo).idOperacao(idOperacao).build();

        // Mock para simular resposta do Repository
        PermissaoEntity entidade = PermissaoEntity.builder().cargo(cargo).operacao(operacao).build();
        Page<PermissaoEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(permissaoRepository.findAllByFilter(idCargo, idOperacao, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<PermissaoResponseDTO> result = permissaoService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getCargo()).isEqualTo(cargo);
        assertThat(result.getContent().getFirst().getOperacao()).isEqualTo(operacao);

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(permissaoRepository).findAllByFilter(idCargo, idOperacao, null);
    }

    @Test
    @DisplayName("Should list one permissao filtered by idCargo")
    void listAllByFilterCase2() {
        // inicializa o filtro
        UUID idCargo = UUID.randomUUID();
        PermissaoFilterDTO filtro = PermissaoFilterDTO.builder().idCargo(idCargo).build();

        // Mock para simular resposta do Repository
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().nome("Operacao").codigo("OP1").build();
        PermissaoEntity entidade = PermissaoEntity.builder().cargo(cargo).operacao(operacao).build();
        Page<PermissaoEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(permissaoRepository.findAllByFilter(idCargo, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<PermissaoResponseDTO> result = permissaoService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getCargo()).isEqualTo(cargo);

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(permissaoRepository).findAllByFilter(idCargo, null, null);
    }

    @Test
    @DisplayName("Should list all permissoes when filter is null")
    void listAllByFilterCase3() {
        // Mock para simular resposta do Repository
        CargoEntity cargo = CargoEntity.builder().nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().nome("Operacao").codigo("OP1").build();
        PermissaoEntity entidade1 = PermissaoEntity.builder().cargo(cargo).operacao(operacao).build();
        PermissaoEntity entidade2 = PermissaoEntity.builder().cargo(cargo).operacao(operacao).build();
        Page<PermissaoEntity> paginaMock = new PageImpl<>(List.of(entidade1, entidade2));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(permissaoRepository.findAllByFilter(null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter passando filtro nulo
        Page<PermissaoResponseDTO> result = permissaoService.listAllByFilter(null, null);

        assertThat(result.getContent()).hasSize(2);

        // verifica se ao chamar a findAllByFilter ele passou todos os parametros nulos
        verify(permissaoRepository).findAllByFilter(null, null, null);
    }

    @Test
    @DisplayName("Should not list permissao when filter does not match")
    void listAllByFilterCase4() {
        // inicializa o filtro
        UUID idCargo = UUID.randomUUID();
        PermissaoFilterDTO filtro = PermissaoFilterDTO.builder().idCargo(idCargo).build();

        // Mock para simular resposta do Repository
        Page<PermissaoEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(permissaoRepository.findAllByFilter(idCargo, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<PermissaoResponseDTO> result = permissaoService.listAllByFilter(null, filtro);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(permissaoRepository).findAllByFilter(idCargo, null, null);
    }

    @Test
    @DisplayName("Should return permissao when id exists")
    void findCase1() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Repository
        CargoEntity cargo = CargoEntity.builder().nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().nome("Operacao").codigo("OP1").build();
        PermissaoEntity entidade = PermissaoEntity.builder().id(id).cargo(cargo).operacao(operacao).build();

        // Quando chamar findById ele retorna o mock entidade
        when(permissaoRepository.findById(id)).thenReturn(Optional.of(entidade));

        // chama a funcao find
        PermissaoResponseDTO result = permissaoService.find(null, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getCargo()).isEqualTo(cargo);
        assertThat(result.getOperacao()).isEqualTo(operacao);

        // verifica se ao chamar a findById ele passou o mesmo id
        verify(permissaoRepository).findById(id);
    }

    @Test
    @DisplayName("Should throw exception when permissao id does not exist")
    void findCase2() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar findById ele retorna vazio
        when(permissaoRepository.findById(id)).thenReturn(Optional.empty());

        // chama a funcao find e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> permissaoService.find(null, id));

        // verifica se ao chamar a findById ele passou o mesmo id
        verify(permissaoRepository).findById(id);
    }

    @Test
    @DisplayName("Should save and return created permissao")
    void createCase1() {
        // ids do cargo e da operacao utilizados na criacao
        UUID idCargo = UUID.randomUUID();
        UUID idOperacao = UUID.randomUUID();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().id(idOperacao).nome("Operacao").codigo("OP1").build();

        // dados para criacao
        PermissaoCreateDTO permissao = PermissaoCreateDTO.builder().idCargo(idCargo).idOperacao(idOperacao).build();

        // Mock para simular que o cargo e a operacao existem
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.of(cargo));
        when(operacaoRepository.findById(idOperacao)).thenReturn(Optional.of(operacao));

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        PermissaoEntity entidadeSalva = PermissaoEntity.builder().id(id).cargo(cargo).operacao(operacao).build();
        when(permissaoRepository.save(any(PermissaoEntity.class))).thenReturn(entidadeSalva);

        // chama a funcao create
        PermissaoResponseDTO result = permissaoService.create(permissao);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getCargo()).isEqualTo(cargo);
        assertThat(result.getOperacao()).isEqualTo(operacao);

        // verifica se buscou o cargo e a operacao antes de criar
        verify(cargoRepository).findById(idCargo);
        verify(operacaoRepository).findById(idOperacao);

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(permissaoRepository).save(argThat(e -> e.getCargo().equals(cargo) && e.getOperacao().equals(operacao)));
    }

    @Test
    @DisplayName("Should throw exception when creating permissao with a cargo that does not exist")
    void createCase2() {
        // id do cargo inexistente
        UUID idCargo = UUID.randomUUID();
        UUID idOperacao = UUID.randomUUID();

        // dados para criacao
        PermissaoCreateDTO permissao = PermissaoCreateDTO.builder().idCargo(idCargo).idOperacao(idOperacao).build();

        // Mock para simular que o cargo nao existe
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> permissaoService.create(permissao));

        // verifica se buscou o cargo antes de tentar criar
        verify(cargoRepository).findById(idCargo);

        // verifica se nunca chegou a buscar a operacao nem a salvar
        verify(operacaoRepository, never()).findById(any(UUID.class));
        verify(permissaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating permissao with an operacao that does not exist")
    void createCase3() {
        // ids do cargo e da operacao inexistente
        UUID idCargo = UUID.randomUUID();
        UUID idOperacao = UUID.randomUUID();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();

        // dados para criacao
        PermissaoCreateDTO permissao = PermissaoCreateDTO.builder().idCargo(idCargo).idOperacao(idOperacao).build();

        // Mock para simular que o cargo existe, mas a operacao nao
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.of(cargo));
        when(operacaoRepository.findById(idOperacao)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> permissaoService.create(permissao));

        // verifica se buscou o cargo e a operacao antes de tentar criar
        verify(cargoRepository).findById(idCargo);
        verify(operacaoRepository).findById(idOperacao);

        // verifica se nunca chegou a salvar, ja que a operacao nao existe
        verify(permissaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update permissao when id, cargo and operacao exist")
    void updateCase1() {
        // ids da permissao, do cargo e da operacao utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();
        UUID idOperacao = UUID.randomUUID();

        // dados para atualizacao
        PermissaoCreateDTO permissao = PermissaoCreateDTO.builder().idCargo(idCargo).idOperacao(idOperacao).build();

        // Mock para simular que a permissao existe
        CargoEntity cargoAntigo = CargoEntity.builder().nome("Cargo Old").build();
        OperacaoEntity operacaoAntiga = OperacaoEntity.builder().nome("Operacao Old").codigo("OPOLD").build();
        PermissaoEntity entidadeExistente = PermissaoEntity.builder().id(id).cargo(cargoAntigo).operacao(operacaoAntiga).build();
        when(permissaoRepository.findById(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o cargo e a operacao existem
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().id(idOperacao).nome("Operacao").codigo("OP1").build();
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.of(cargo));
        when(operacaoRepository.findById(idOperacao)).thenReturn(Optional.of(operacao));

        // Mock para simular resposta do save
        PermissaoEntity entidadeAtualizada = PermissaoEntity.builder().id(id).cargo(cargo).operacao(operacao).build();
        when(permissaoRepository.save(any(PermissaoEntity.class))).thenReturn(entidadeAtualizada);

        // chama a funcao update
        PermissaoResponseDTO result = permissaoService.update(permissao, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getCargo()).isEqualTo(cargo);
        assertThat(result.getOperacao()).isEqualTo(operacao);

        // verifica se buscou a permissao, o cargo e a operacao antes de atualizar
        verify(permissaoRepository).findById(id);
        verify(cargoRepository).findById(idCargo);
        verify(operacaoRepository).findById(idOperacao);

        // verifica se salvou a entidade com os dados atualizados
        verify(permissaoRepository).save(argThat(e -> e.getId().equals(id) && e.getCargo().equals(cargo) && e.getOperacao().equals(operacao)));
    }

    @Test
    @DisplayName("Should throw exception when updating a permissao that does not exist")
    void updateCase2() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        PermissaoCreateDTO permissao = PermissaoCreateDTO.builder().idCargo(UUID.randomUUID()).idOperacao(UUID.randomUUID()).build();

        // Mock para simular que a permissao nao existe
        when(permissaoRepository.findById(id)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> permissaoService.update(permissao, id));

        // verifica se buscou a permissao antes de tentar atualizar
        verify(permissaoRepository).findById(id);

        // verifica se nunca chegou a buscar o cargo, a operacao nem a salvar
        verify(cargoRepository, never()).findById(any(UUID.class));
        verify(operacaoRepository, never()).findById(any(UUID.class));
        verify(permissaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating permissao with a cargo that does not exist")
    void updateCase3() {
        // ids da permissao e do cargo utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();
        UUID idOperacao = UUID.randomUUID();
        PermissaoCreateDTO permissao = PermissaoCreateDTO.builder().idCargo(idCargo).idOperacao(idOperacao).build();

        // Mock para simular que a permissao existe
        CargoEntity cargoAntigo = CargoEntity.builder().nome("Cargo Old").build();
        OperacaoEntity operacaoAntiga = OperacaoEntity.builder().nome("Operacao Old").codigo("OPOLD").build();
        PermissaoEntity entidadeExistente = PermissaoEntity.builder().id(id).cargo(cargoAntigo).operacao(operacaoAntiga).build();
        when(permissaoRepository.findById(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o cargo nao existe
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> permissaoService.update(permissao, id));

        // verifica se buscou a permissao e o cargo antes de tentar atualizar
        verify(permissaoRepository).findById(id);
        verify(cargoRepository).findById(idCargo);

        // verifica se nunca chegou a buscar a operacao nem a salvar, ja que o cargo nao existe
        verify(operacaoRepository, never()).findById(any(UUID.class));
        verify(permissaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating permissao with an operacao that does not exist")
    void updateCase4() {
        // ids da permissao, do cargo e da operacao utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();
        UUID idOperacao = UUID.randomUUID();
        PermissaoCreateDTO permissao = PermissaoCreateDTO.builder().idCargo(idCargo).idOperacao(idOperacao).build();

        // Mock para simular que a permissao existe
        CargoEntity cargoAntigo = CargoEntity.builder().nome("Cargo Old").build();
        OperacaoEntity operacaoAntiga = OperacaoEntity.builder().nome("Operacao Old").codigo("OPOLD").build();
        PermissaoEntity entidadeExistente = PermissaoEntity.builder().id(id).cargo(cargoAntigo).operacao(operacaoAntiga).build();
        when(permissaoRepository.findById(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o cargo existe, mas a operacao nao
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.of(cargo));
        when(operacaoRepository.findById(idOperacao)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> permissaoService.update(permissao, id));

        // verifica se buscou a permissao, o cargo e a operacao antes de tentar atualizar
        verify(permissaoRepository).findById(id);
        verify(cargoRepository).findById(idCargo);
        verify(operacaoRepository).findById(idOperacao);

        // verifica se nunca chegou a salvar, ja que a operacao nao existe
        verify(permissaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete permissao by id")
    void deleteCase1() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // chama a funcao delete
        permissaoService.delete(id);

        // verifica se excluiu o id correto
        verify(permissaoRepository).deleteById(id);
    }
}
