package com.nowbox.nowbox_api.modules.unidade.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.estado.repository.IEstadoRepository;
import com.nowbox.nowbox_api.modules.unidade.dto.UnidadeCreateDTO;
import com.nowbox.nowbox_api.modules.unidade.dto.UnidadeFilterDTO;
import com.nowbox.nowbox_api.modules.unidade.dto.UnidadeResponseDTO;
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
class UnidadeServiceTest {

    @Mock
    private IUnidadeRepository unidadeRepository;

    @Mock
    private IEstadoRepository estadoRepository;

    @InjectMocks
    private UnidadeService unidadeService;

    @Test
    @DisplayName("Should list one unidade filtered by idEstado, nome, cnpj and cidade")
    void listAllByFilterCase1() {
        // id do estado utilizado no filtro
        UUID idEstado = UUID.randomUUID();
        EstadoEntity estado = EstadoEntity.builder().id(idEstado).nome("Estado").uf("ES").build();

        // inicializa o filtro
        UnidadeFilterDTO filtro = UnidadeFilterDTO.builder().idEstado(idEstado).nome("Unidade Test").cnpj("12345678901234").cidade("Cidade Test").build();

        // Mock para simular resposta do Repository
        UnidadeEntity entidade = UnidadeEntity.builder().nome("Unidade Test").cnpj("12345678901234").cidade("Cidade Test").estado(estado).build();
        Page<UnidadeEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(unidadeRepository.findAllByFilter(idEstado, "Unidade Test", "12345678901234", "Cidade Test", null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<UnidadeResponseDTO> result = unidadeService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Unidade Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(unidadeRepository).findAllByFilter(idEstado, "Unidade Test", "12345678901234", "Cidade Test", null);
    }

    @Test
    @DisplayName("Should list one unidade filtered by nome")
    void listAllByFilterCase2() {
        // inicializa o filtro
        UnidadeFilterDTO filtro = UnidadeFilterDTO.builder().nome("Unidade Test").build();

        // Mock para simular resposta do Repository
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("ES").build();
        UnidadeEntity entidade = UnidadeEntity.builder().nome("Unidade Test").cidade("Cidade Test").estado(estado).build();
        Page<UnidadeEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(unidadeRepository.findAllByFilter(null, "Unidade Test", null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<UnidadeResponseDTO> result = unidadeService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Unidade Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(unidadeRepository).findAllByFilter(null, "Unidade Test", null, null, null);
    }

    @Test
    @DisplayName("Should list all unidades when filter is null")
    void listAllByFilterCase3() {
        // Mock para simular resposta do Repository
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("ES").build();
        UnidadeEntity entidade1 = UnidadeEntity.builder().nome("Unidade Test 1").cidade("Cidade 1").estado(estado).build();
        UnidadeEntity entidade2 = UnidadeEntity.builder().nome("Unidade Test 2").cidade("Cidade 2").estado(estado).build();
        Page<UnidadeEntity> paginaMock = new PageImpl<>(List.of(entidade1, entidade2));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(unidadeRepository.findAllByFilter(null, null, null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter passando filtro nulo
        Page<UnidadeResponseDTO> result = unidadeService.listAllByFilter(null, null);

        assertThat(result.getContent()).hasSize(2);

        // verifica se ao chamar a findAllByFilter ele passou todos os parametros nulos
        verify(unidadeRepository).findAllByFilter(null, null, null, null, null);
    }

    @Test
    @DisplayName("Should not list unidade when filter does not match")
    void listAllByFilterCase4() {
        // inicializa o filtro
        UnidadeFilterDTO filtro = UnidadeFilterDTO.builder().nome("Unidade Inexistente").cidade("Cidade Inexistente").build();

        // Mock para simular resposta do Repository
        Page<UnidadeEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(unidadeRepository.findAllByFilter(null, "Unidade Inexistente", null, "Cidade Inexistente", null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<UnidadeResponseDTO> result = unidadeService.listAllByFilter(null, filtro);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(unidadeRepository).findAllByFilter(null, "Unidade Inexistente", null, "Cidade Inexistente", null);
    }

    @Test
    @DisplayName("Should return unidade when id exists")
    void findCase1() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Repository
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("ES").build();
        UnidadeEntity entidade = UnidadeEntity.builder().id(id).nome("Unidade Test").cidade("Cidade Test").estado(estado).build();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna o mock entidade
        when(unidadeRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidade));

        // chama a funcao find
        UnidadeResponseDTO result = unidadeService.find(null, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Unidade Test");
        assertThat(result.getCidade()).isEqualTo("Cidade Test");

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(unidadeRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should throw exception when unidade id does not exist")
    void findCase2() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna vazio
        when(unidadeRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao find e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> unidadeService.find(null, id));

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(unidadeRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should save and return created unidade")
    void createCase1() {
        // id do estado utilizado na criacao
        UUID idEstado = UUID.randomUUID();
        EstadoEntity estado = EstadoEntity.builder().id(idEstado).nome("Estado").uf("ES").build();

        // dados para criacao
        UnidadeCreateDTO unidade = UnidadeCreateDTO.builder()
                .idEstado(idEstado)
                .nome("Unidade Test")
                .cnpj("12345678901234")
                .endereco("Rua Test")
                .numero("100")
                .complemento("Sala 1")
                .bairro("Bairro Test")
                .cep("12345678")
                .cidade("Cidade Test")
                .build();

        // Mock para simular que o estado existe
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.of(estado));

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        UnidadeEntity entidadeSalva = UnidadeEntity.builder()
                .id(id)
                .nome("Unidade Test")
                .cnpj("12345678901234")
                .endereco("Rua Test")
                .numero("100")
                .complemento("Sala 1")
                .bairro("Bairro Test")
                .cep("12345678")
                .cidade("Cidade Test")
                .estado(estado)
                .createdAt(LocalDateTime.now())
                .build();
        when(unidadeRepository.save(any(UnidadeEntity.class))).thenReturn(entidadeSalva);

        // chama a funcao create
        UnidadeResponseDTO result = unidadeService.create(unidade);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Unidade Test");
        assertThat(result.getCnpj()).isEqualTo("12345678901234");
        assertThat(result.getEstado()).isEqualTo(estado);

        // verifica se buscou o estado antes de criar
        verify(estadoRepository).findById(idEstado);

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(unidadeRepository).save(argThat(e -> e.getNome().equals("Unidade Test") && e.getCnpj().equals("12345678901234") && e.getEstado().equals(estado)));
    }

    @Test
    @DisplayName("Should throw exception when creating unidade with an estado that does not exist")
    void createCase2() {
        // id do estado inexistente
        UUID idEstado = UUID.randomUUID();

        // dados para criacao
        UnidadeCreateDTO unidade = UnidadeCreateDTO.builder().idEstado(idEstado).nome("Unidade Test").build();

        // Mock para simular que o estado nao existe
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> unidadeService.create(unidade));

        // verifica se buscou o estado antes de tentar criar
        verify(estadoRepository).findById(idEstado);

        // verifica se nunca chegou a salvar, ja que o estado nao existe
        verify(unidadeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update unidade when id and estado exist")
    void updateCase1() {
        // id da unidade e do estado utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idEstado = UUID.randomUUID();

        // dados para atualizacao
        UnidadeCreateDTO unidade = UnidadeCreateDTO.builder().idEstado(idEstado).nome("Unidade Test").cidade("Cidade Test").build();

        // Mock para simular que a unidade existe
        EstadoEntity estadoAntigo = EstadoEntity.builder().nome("Estado Old").uf("EO").build();
        UnidadeEntity entidadeExistente = UnidadeEntity.builder().id(id).nome("Unidade Old").cidade("Cidade Old").estado(estadoAntigo).createdAt(LocalDateTime.now()).build();
        when(unidadeRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o estado existe
        EstadoEntity estado = EstadoEntity.builder().id(idEstado).nome("Estado").uf("ES").build();
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.of(estado));

        // Mock para simular resposta do save
        UnidadeEntity entidadeAtualizada = UnidadeEntity.builder().id(id).nome("Unidade Test").cidade("Cidade Test").estado(estado).build();
        when(unidadeRepository.save(any(UnidadeEntity.class))).thenReturn(entidadeAtualizada);

        // chama a funcao update
        UnidadeResponseDTO result = unidadeService.update(unidade, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Unidade Test");
        assertThat(result.getCidade()).isEqualTo("Cidade Test");
        assertThat(result.getEstado()).isEqualTo(estado);

        // verifica se buscou a unidade e o estado antes de atualizar
        verify(unidadeRepository).findByIdAndDeletedAtIsNull(id);
        verify(estadoRepository).findById(idEstado);

        // verifica se salvou a entidade com os dados atualizados
        verify(unidadeRepository).save(argThat(e -> e.getId().equals(id) && e.getNome().equals("Unidade Test") && e.getCidade().equals("Cidade Test") && e.getEstado().equals(estado)));
    }

    @Test
    @DisplayName("Should throw exception when updating a unidade that does not exist")
    void updateCase2() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UnidadeCreateDTO unidade = UnidadeCreateDTO.builder().idEstado(UUID.randomUUID()).nome("Unidade Test").build();

        // Mock para simular que a unidade nao existe
        when(unidadeRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> unidadeService.update(unidade, id));

        // verifica se buscou a unidade antes de tentar atualizar
        verify(unidadeRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se nunca chegou a buscar o estado nem a salvar
        verify(estadoRepository, never()).findById(any(UUID.class));
        verify(unidadeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating unidade with an estado that does not exist")
    void updateCase3() {
        // id da unidade e do estado utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idEstado = UUID.randomUUID();
        UnidadeCreateDTO unidade = UnidadeCreateDTO.builder().idEstado(idEstado).nome("Unidade Test").build();

        // Mock para simular que a unidade existe
        EstadoEntity estadoAntigo = EstadoEntity.builder().nome("Estado Old").uf("EO").build();
        UnidadeEntity entidadeExistente = UnidadeEntity.builder().id(id).nome("Unidade Old").estado(estadoAntigo).build();
        when(unidadeRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o estado nao existe
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> unidadeService.update(unidade, id));

        // verifica se buscou a unidade e o estado antes de tentar atualizar
        verify(unidadeRepository).findByIdAndDeletedAtIsNull(id);
        verify(estadoRepository).findById(idEstado);

        // verifica se nunca chegou a salvar, ja que o estado nao existe
        verify(unidadeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete unidade by id")
    void deleteCase1() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que a unidade existe
        UnidadeEntity entidadeExistente = UnidadeEntity.builder().id(id).nome("Unidade Test").build();
        when(unidadeRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // chama a funcao delete
        unidadeService.delete(id);

        // verifica se buscou a unidade antes de excluir
        verify(unidadeRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se salvou a entidade com o deletedAt preenchido
        verify(unidadeRepository).save(argThat(e -> e.getId().equals(id) && e.getDeletedAt() != null));
    }

    @Test
    @DisplayName("Should throw exception when deleting a unidade that does not exist")
    void deleteCase2() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que a unidade nao existe
        when(unidadeRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao delete e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> unidadeService.delete(id));

        // verifica se nunca chegou a salvar, ja que a unidade nao existe
        verify(unidadeRepository, never()).save(any());
    }
}
