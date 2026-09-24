package com.nowbox.nowbox_api.modules.permissao.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.cargo.repository.ICargoRepository;
import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import com.nowbox.nowbox_api.modules.operacao.repository.IOperacaoRepository;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoCreateDTO;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoFilterDTO;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoLoteDTO;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
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
    @DisplayName("Should create the new permissoes and remove the ones not sent when syncing a cargo")
    void syncByCargoCase1() {
        // cargo e operacoes utilizados na sincronizacao
        UUID idCargo = UUID.randomUUID();
        UUID idMantida = UUID.randomUUID();
        UUID idNova = UUID.randomUUID();
        UUID idRemovida = UUID.randomUUID();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        OperacaoEntity mantida = OperacaoEntity.builder().id(idMantida).nome("Mantida").codigo("OP1").build();
        OperacaoEntity nova = OperacaoEntity.builder().id(idNova).nome("Nova").codigo("OP2").build();
        OperacaoEntity removida = OperacaoEntity.builder().id(idRemovida).nome("Removida").codigo("OP3").build();

        // dados enviados: mantem a operacao OP1 e adiciona a OP2, removendo a OP3
        PermissaoLoteDTO lote = PermissaoLoteDTO.builder().idsOperacao(Set.of(idMantida, idNova)).build();

        // Mock para simular o cargo, as operacoes e as permissoes ja existentes
        PermissaoEntity permissaoMantida = PermissaoEntity.builder().id(UUID.randomUUID()).cargo(cargo).operacao(mantida).build();
        PermissaoEntity permissaoRemovida = PermissaoEntity.builder().id(UUID.randomUUID()).cargo(cargo).operacao(removida).build();
        PermissaoEntity permissaoNova = PermissaoEntity.builder().id(UUID.randomUUID()).cargo(cargo).operacao(nova).build();
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.of(cargo));
        when(operacaoRepository.findAllById(any())).thenReturn(List.of(mantida, nova));
        when(permissaoRepository.findAllByCargoId(idCargo))
                .thenReturn(List.of(permissaoMantida, permissaoRemovida))
                .thenReturn(List.of(permissaoMantida, permissaoNova));

        // chama a funcao syncByCargo
        List<PermissaoResponseDTO> result = permissaoService.syncByCargo(idCargo, lote);

        // retorna o estado final das permissoes do cargo
        assertThat(result).hasSize(2);
        assertThat(result).extracting(r -> r.getOperacao().getId()).containsExactlyInAnyOrder(idMantida, idNova);

        // verifica se removeu apenas a permissao que nao foi enviada
        verify(permissaoRepository).deleteAll(List.of(permissaoRemovida));

        // verifica se criou apenas a permissao que ainda nao existia
        verify(permissaoRepository).saveAll(argThat((Iterable<PermissaoEntity> e) -> {
            List<PermissaoEntity> lista = new java.util.ArrayList<>();
            e.forEach(lista::add);
            return lista.size() == 1 && lista.getFirst().getOperacao().equals(nova) && lista.getFirst().getCargo().equals(cargo);
        }));
    }

    @Test
    @DisplayName("Should remove every permissao when syncing a cargo with no operacoes")
    void syncByCargoCase2() {
        // cargo com uma permissao existente
        UUID idCargo = UUID.randomUUID();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().id(UUID.randomUUID()).nome("Operacao").codigo("OP1").build();
        PermissaoEntity existente = PermissaoEntity.builder().id(UUID.randomUUID()).cargo(cargo).operacao(operacao).build();

        // dados enviados sem nenhuma operacao
        PermissaoLoteDTO lote = PermissaoLoteDTO.builder().idsOperacao(Set.of()).build();

        // Mock para simular o cargo e as permissoes existentes
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.of(cargo));
        when(operacaoRepository.findAllById(any())).thenReturn(List.of());
        when(permissaoRepository.findAllByCargoId(idCargo)).thenReturn(List.of(existente)).thenReturn(List.of());

        // chama a funcao syncByCargo
        List<PermissaoResponseDTO> result = permissaoService.syncByCargo(idCargo, lote);

        assertThat(result).isEmpty();

        // verifica se removeu a permissao existente e nao criou nenhuma
        verify(permissaoRepository).deleteAll(List.of(existente));
        verify(permissaoRepository).saveAll(List.of());
    }

    @Test
    @DisplayName("Should throw exception when syncing a cargo that does not exist")
    void syncByCargoCase3() {
        // id do cargo inexistente
        UUID idCargo = UUID.randomUUID();
        PermissaoLoteDTO lote = PermissaoLoteDTO.builder().idsOperacao(Set.of(UUID.randomUUID())).build();

        // Mock para simular que o cargo nao existe
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.empty());

        // chama a funcao syncByCargo e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> permissaoService.syncByCargo(idCargo, lote));

        // verifica se nunca chegou a buscar as operacoes nem a alterar as permissoes
        verify(operacaoRepository, never()).findAllById(anyIterable());
        verify(permissaoRepository, never()).deleteAll(anyIterable());
        verify(permissaoRepository, never()).saveAll(anyIterable());
    }

    @Test
    @DisplayName("Should throw exception when syncing a cargo with an operacao that does not exist")
    void syncByCargoCase4() {
        // cargo existente e operacao inexistente
        UUID idCargo = UUID.randomUUID();
        UUID idOperacaoExistente = UUID.randomUUID();
        UUID idOperacaoInexistente = UUID.randomUUID();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().id(idOperacaoExistente).nome("Operacao").codigo("OP1").build();
        PermissaoLoteDTO lote = PermissaoLoteDTO.builder().idsOperacao(Set.of(idOperacaoExistente, idOperacaoInexistente)).build();

        // Mock para simular que apenas uma das operacoes existe
        when(cargoRepository.findById(idCargo)).thenReturn(Optional.of(cargo));
        when(operacaoRepository.findAllById(any())).thenReturn(List.of(operacao));

        // chama a funcao syncByCargo e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> permissaoService.syncByCargo(idCargo, lote));

        // verifica se nunca chegou a alterar as permissoes
        verify(permissaoRepository, never()).deleteAll(anyIterable());
        verify(permissaoRepository, never()).saveAll(anyIterable());
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
