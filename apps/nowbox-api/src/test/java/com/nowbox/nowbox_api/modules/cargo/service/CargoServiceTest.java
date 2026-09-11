package com.nowbox.nowbox_api.modules.cargo.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cargo.dto.CargoCreateDTO;
import com.nowbox.nowbox_api.modules.cargo.dto.CargoFilterDTO;
import com.nowbox.nowbox_api.modules.cargo.dto.CargoResponseDTO;
import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.cargo.repository.ICargoRepository;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import com.nowbox.nowbox_api.modules.unidade.repository.IUnidadeRepository;
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
class CargoServiceTest {

    @Mock
    private ICargoRepository cargoRepository;

    @Mock
    private IUnidadeRepository unidadeRepository;

    @InjectMocks
    private CargoService cargoService;

    @Test
    @DisplayName("Should list one cargo filtered by idUnidade and nome")
    void listAllByFilterCase1() {
        // id da unidade utilizado no filtro
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade").build();

        // inicializa o filtro
        CargoFilterDTO filtro = CargoFilterDTO.builder().idUnidade(idUnidade).nome("Cargo Test").build();

        // Mock para simular resposta do Repository
        CargoEntity entidade = CargoEntity.builder().nome("Cargo Test").unidade(unidade).build();
        Page<CargoEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(cargoRepository.findAllByFilter(idUnidade, "Cargo Test", null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<CargoResponseDTO> result = cargoService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Cargo Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(cargoRepository).findAllByFilter(idUnidade, "Cargo Test", null);
    }

    @Test
    @DisplayName("Should list one cargo filtered by nome")
    void listAllByFilterCase2() {
        // inicializa o filtro
        CargoFilterDTO filtro = CargoFilterDTO.builder().nome("Cargo Test").build();

        // Mock para simular resposta do Repository
        UnidadeEntity unidade = UnidadeEntity.builder().nome("Unidade").build();
        CargoEntity entidade = CargoEntity.builder().nome("Cargo Test").unidade(unidade).build();
        Page<CargoEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(cargoRepository.findAllByFilter(null, "Cargo Test", null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<CargoResponseDTO> result = cargoService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Cargo Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(cargoRepository).findAllByFilter(null, "Cargo Test", null);
    }

    @Test
    @DisplayName("Should list all cargos when filter is null")
    void listAllByFilterCase3() {
        // Mock para simular resposta do Repository
        UnidadeEntity unidade = UnidadeEntity.builder().nome("Unidade").build();
        CargoEntity entidade1 = CargoEntity.builder().nome("Cargo Test 1").unidade(unidade).build();
        CargoEntity entidade2 = CargoEntity.builder().nome("Cargo Test 2").unidade(unidade).build();
        Page<CargoEntity> paginaMock = new PageImpl<>(List.of(entidade1, entidade2));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(cargoRepository.findAllByFilter(null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter passando filtro nulo
        Page<CargoResponseDTO> result = cargoService.listAllByFilter(null, null);

        assertThat(result.getContent()).hasSize(2);

        // verifica se ao chamar a findAllByFilter ele passou todos os parametros nulos
        verify(cargoRepository).findAllByFilter(null, null, null);
    }

    @Test
    @DisplayName("Should not list cargo when filter does not match")
    void listAllByFilterCase4() {
        // inicializa o filtro
        CargoFilterDTO filtro = CargoFilterDTO.builder().nome("Cargo Inexistente").build();

        // Mock para simular resposta do Repository
        Page<CargoEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(cargoRepository.findAllByFilter(null, "Cargo Inexistente", null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<CargoResponseDTO> result = cargoService.listAllByFilter(null, filtro);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(cargoRepository).findAllByFilter(null, "Cargo Inexistente", null);
    }

    @Test
    @DisplayName("Should return cargo when id exists")
    void findCase1() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Repository
        UnidadeEntity unidade = UnidadeEntity.builder().nome("Unidade").build();
        CargoEntity entidade = CargoEntity.builder().id(id).nome("Cargo Test").unidade(unidade).build();

        // Quando chamar findById ele retorna o mock entidade
        when(cargoRepository.findById(id)).thenReturn(Optional.of(entidade));

        // chama a funcao find
        CargoResponseDTO result = cargoService.find(null, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Cargo Test");

        // verifica se ao chamar a findById ele passou o mesmo id
        verify(cargoRepository).findById(id);
    }

    @Test
    @DisplayName("Should throw exception when cargo id does not exist")
    void findCase2() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar findById ele retorna vazio
        when(cargoRepository.findById(id)).thenReturn(Optional.empty());

        // chama a funcao find e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> cargoService.find(null, id));

        // verifica se ao chamar a findById ele passou o mesmo id
        verify(cargoRepository).findById(id);
    }

    @Test
    @DisplayName("Should save and return created cargo")
    void createCase1() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade").build();

        // dados para criacao
        CargoCreateDTO cargo = CargoCreateDTO.builder().idUnidade(idUnidade).nome("Cargo Test").build();

        // Mock para simular que a unidade existe
        when(unidadeRepository.findById(idUnidade)).thenReturn(Optional.of(unidade));

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        CargoEntity entidadeSalva = CargoEntity.builder().id(id).nome("Cargo Test").unidade(unidade).build();
        when(cargoRepository.save(any(CargoEntity.class))).thenReturn(entidadeSalva);

        // chama a funcao create
        CargoResponseDTO result = cargoService.create(cargo);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Cargo Test");
        assertThat(result.getUnidade()).isEqualTo(unidade);

        // verifica se buscou a unidade antes de criar
        verify(unidadeRepository).findById(idUnidade);

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(cargoRepository).save(argThat(e -> e.getNome().equals("Cargo Test") && e.getUnidade().equals(unidade)));
    }

    @Test
    @DisplayName("Should throw exception when creating cargo with a unidade that does not exist")
    void createCase2() {
        // id da unidade inexistente
        UUID idUnidade = UUID.randomUUID();

        // dados para criacao
        CargoCreateDTO cargo = CargoCreateDTO.builder().idUnidade(idUnidade).nome("Cargo Test").build();

        // Mock para simular que a unidade nao existe
        when(unidadeRepository.findById(idUnidade)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> cargoService.create(cargo));

        // verifica se buscou a unidade antes de tentar criar
        verify(unidadeRepository).findById(idUnidade);

        // verifica se nunca chegou a salvar, ja que a unidade nao existe
        verify(cargoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update cargo when id and unidade exist")
    void updateCase1() {
        // id do cargo e da unidade utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idUnidade = UUID.randomUUID();

        // dados para atualizacao
        CargoCreateDTO cargo = CargoCreateDTO.builder().idUnidade(idUnidade).nome("Cargo Test").build();

        // Mock para simular que o cargo existe
        UnidadeEntity unidadeAntiga = UnidadeEntity.builder().nome("Unidade Old").build();
        CargoEntity entidadeExistente = CargoEntity.builder().id(id).nome("Cargo Old").unidade(unidadeAntiga).build();
        when(cargoRepository.findById(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que a unidade existe
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade").build();
        when(unidadeRepository.findById(idUnidade)).thenReturn(Optional.of(unidade));

        // Mock para simular resposta do save
        CargoEntity entidadeAtualizada = CargoEntity.builder().id(id).nome("Cargo Test").unidade(unidade).build();
        when(cargoRepository.save(any(CargoEntity.class))).thenReturn(entidadeAtualizada);

        // chama a funcao update
        CargoResponseDTO result = cargoService.update(cargo, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Cargo Test");
        assertThat(result.getUnidade()).isEqualTo(unidade);

        // verifica se buscou o cargo e a unidade antes de atualizar
        verify(cargoRepository).findById(id);
        verify(unidadeRepository).findById(idUnidade);

        // verifica se salvou a entidade com os dados atualizados
        verify(cargoRepository).save(argThat(e -> e.getId().equals(id) && e.getNome().equals("Cargo Test") && e.getUnidade().equals(unidade)));
    }

    @Test
    @DisplayName("Should throw exception when updating a cargo that does not exist")
    void updateCase2() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        CargoCreateDTO cargo = CargoCreateDTO.builder().idUnidade(UUID.randomUUID()).nome("Cargo Test").build();

        // Mock para simular que o cargo nao existe
        when(cargoRepository.findById(id)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> cargoService.update(cargo, id));

        // verifica se buscou o cargo antes de tentar atualizar
        verify(cargoRepository).findById(id);

        // verifica se nunca chegou a buscar a unidade nem a salvar
        verify(unidadeRepository, never()).findById(any(UUID.class));
        verify(cargoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating cargo with a unidade that does not exist")
    void updateCase3() {
        // id do cargo e da unidade utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idUnidade = UUID.randomUUID();
        CargoCreateDTO cargo = CargoCreateDTO.builder().idUnidade(idUnidade).nome("Cargo Test").build();

        // Mock para simular que o cargo existe
        UnidadeEntity unidadeAntiga = UnidadeEntity.builder().nome("Unidade Old").build();
        CargoEntity entidadeExistente = CargoEntity.builder().id(id).nome("Cargo Old").unidade(unidadeAntiga).build();
        when(cargoRepository.findById(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que a unidade nao existe
        when(unidadeRepository.findById(idUnidade)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> cargoService.update(cargo, id));

        // verifica se buscou o cargo e a unidade antes de tentar atualizar
        verify(cargoRepository).findById(id);
        verify(unidadeRepository).findById(idUnidade);

        // verifica se nunca chegou a salvar, ja que a unidade nao existe
        verify(cargoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete cargo by id")
    void deleteCase1() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // chama a funcao delete
        cargoService.delete(id);

        // verifica se excluiu o id correto
        verify(cargoRepository).deleteById(id);
    }
}
