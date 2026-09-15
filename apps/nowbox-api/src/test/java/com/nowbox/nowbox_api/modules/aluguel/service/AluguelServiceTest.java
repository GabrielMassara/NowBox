package com.nowbox.nowbox_api.modules.aluguel.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelCreateDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelFilterDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelResponseDTO;
import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.repository.IClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
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
class AluguelServiceTest {

    @Mock
    private IAluguelRepository aluguelRepository;

    @Mock
    private IBoxRepository boxRepository;

    @Mock
    private IClienteRepository clienteRepository;

    @InjectMocks
    private AluguelService aluguelService;

    @Test
    @DisplayName("Should list one aluguel filtered by idBox, idCliente and status")
    void listAllByFilterCase1() {
        // ids utilizados no filtro
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = BoxEntity.builder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();

        // inicializa o filtro
        AluguelFilterDTO filtro = AluguelFilterDTO.builder().idBox(idBox).idCliente(idCliente).status(true).build();

        // Mock para simular resposta do Repository
        AluguelEntity entidade = AluguelEntity.builder().box(box).cliente(cliente).valor(BigDecimal.valueOf(150.00)).status(true).build();
        Page<AluguelEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(aluguelRepository.findAllByFilter(idBox, idCliente, true, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<AluguelResponseDTO> result = aluguelService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getBox()).isEqualTo(box);
        assertThat(result.getContent().getFirst().getCliente()).isEqualTo(cliente);
        assertThat(result.getContent().getFirst().getStatus()).isTrue();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(aluguelRepository).findAllByFilter(idBox, idCliente, true, null);
    }

    @Test
    @DisplayName("Should list one aluguel filtered by idBox")
    void listAllByFilterCase2() {
        // inicializa o filtro
        UUID idBox = UUID.randomUUID();
        AluguelFilterDTO filtro = AluguelFilterDTO.builder().idBox(idBox).build();

        // Mock para simular resposta do Repository
        BoxEntity box = BoxEntity.builder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().nome("Cliente").build();
        AluguelEntity entidade = AluguelEntity.builder().box(box).cliente(cliente).build();
        Page<AluguelEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(aluguelRepository.findAllByFilter(idBox, null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<AluguelResponseDTO> result = aluguelService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getBox()).isEqualTo(box);

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(aluguelRepository).findAllByFilter(idBox, null, null, null);
    }

    @Test
    @DisplayName("Should list all alugueis when filter is null")
    void listAllByFilterCase3() {
        // Mock para simular resposta do Repository
        BoxEntity box = BoxEntity.builder().numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().nome("Cliente").build();
        AluguelEntity entidade1 = AluguelEntity.builder().box(box).cliente(cliente).build();
        AluguelEntity entidade2 = AluguelEntity.builder().box(box).cliente(cliente).build();
        Page<AluguelEntity> paginaMock = new PageImpl<>(List.of(entidade1, entidade2));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(aluguelRepository.findAllByFilter(null, null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter passando filtro nulo
        Page<AluguelResponseDTO> result = aluguelService.listAllByFilter(null, null);

        assertThat(result.getContent()).hasSize(2);

        // verifica se ao chamar a findAllByFilter ele passou todos os parametros nulos
        verify(aluguelRepository).findAllByFilter(null, null, null, null);
    }

    @Test
    @DisplayName("Should not list aluguel when filter does not match")
    void listAllByFilterCase4() {
        // inicializa o filtro
        UUID idBox = UUID.randomUUID();
        AluguelFilterDTO filtro = AluguelFilterDTO.builder().idBox(idBox).build();

        // Mock para simular resposta do Repository
        Page<AluguelEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(aluguelRepository.findAllByFilter(idBox, null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<AluguelResponseDTO> result = aluguelService.listAllByFilter(null, filtro);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(aluguelRepository).findAllByFilter(idBox, null, null, null);
    }

    @Test
    @DisplayName("Should return aluguel when id exists")
    void findCase1() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Repository
        BoxEntity box = BoxEntity.builder().numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().nome("Cliente").build();
        AluguelEntity entidade = AluguelEntity.builder().id(id).box(box).cliente(cliente).build();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna o mock entidade
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidade));

        // chama a funcao find
        AluguelResponseDTO result = aluguelService.find(null, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getBox()).isEqualTo(box);
        assertThat(result.getCliente()).isEqualTo(cliente);

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(aluguelRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should throw exception when aluguel id does not exist")
    void findCase2() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna vazio
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao find e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> aluguelService.find(null, id));

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(aluguelRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should save and return created aluguel")
    void createCase1() {
        // ids do box e do cliente utilizados na criacao
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = BoxEntity.builder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();

        // dados para criacao
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder()
                .idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(150.00)).observacao("Observação teste").status(true).build();

        // Mock para simular que o box e o cliente existem
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        AluguelEntity entidadeSalva = AluguelEntity.builder()
                .id(id).box(box).cliente(cliente).valor(BigDecimal.valueOf(150.00))
                .observacao("Observação teste").status(true).createdAt(LocalDateTime.now()).build();
        when(aluguelRepository.save(any(AluguelEntity.class))).thenReturn(entidadeSalva);

        // chama a funcao create
        AluguelResponseDTO result = aluguelService.create(aluguel);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getBox()).isEqualTo(box);
        assertThat(result.getCliente()).isEqualTo(cliente);
        assertThat(result.getValor()).isEqualTo(BigDecimal.valueOf(150.00));
        assertThat(result.getObservacao()).isEqualTo("Observação teste");
        assertThat(result.getStatus()).isTrue();

        // verifica se buscou o box e o cliente antes de criar
        verify(boxRepository).findByIdAndDeletedAtIsNull(idBox);
        verify(clienteRepository).findByIdAndDeletedAtIsNull(idCliente);

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(aluguelRepository).save(argThat(e -> e.getBox().equals(box) && e.getCliente().equals(cliente) && e.getValor().equals(BigDecimal.valueOf(150.00)) && e.getStatus()));
    }

    @Test
    @DisplayName("Should throw exception when creating aluguel with a box that does not exist")
    void createCase2() {
        // id do box inexistente
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        // dados para criacao
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).build();

        // Mock para simular que o box nao existe
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> aluguelService.create(aluguel));

        // verifica se buscou o box antes de tentar criar
        verify(boxRepository).findByIdAndDeletedAtIsNull(idBox);

        // verifica se nunca chegou a buscar o cliente nem a salvar
        verify(clienteRepository, never()).findByIdAndDeletedAtIsNull(any(UUID.class));
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating aluguel with a cliente that does not exist")
    void createCase3() {
        // ids do box e do cliente inexistente
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = BoxEntity.builder().id(idBox).numero("101").build();

        // dados para criacao
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).build();

        // Mock para simular que o box existe, mas o cliente nao
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> aluguelService.create(aluguel));

        // verifica se buscou o box e o cliente antes de tentar criar
        verify(boxRepository).findByIdAndDeletedAtIsNull(idBox);
        verify(clienteRepository).findByIdAndDeletedAtIsNull(idCliente);

        // verifica se nunca chegou a salvar, ja que o cliente nao existe
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update aluguel when id, box and cliente exist")
    void updateCase1() {
        // ids do aluguel, do box e do cliente utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        // dados para atualizacao
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(200.00)).status(false).build();

        // Mock para simular que o aluguel existe
        BoxEntity boxAntigo = BoxEntity.builder().numero("100").build();
        ClienteEntity clienteAntigo = ClienteEntity.builder().nome("Cliente Old").build();
        AluguelEntity entidadeExistente = AluguelEntity.builder().id(id).box(boxAntigo).cliente(clienteAntigo).valor(BigDecimal.valueOf(150.00)).status(true).createdAt(LocalDateTime.now()).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o box e o cliente existem
        BoxEntity box = BoxEntity.builder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));

        // Mock para simular resposta do save
        AluguelEntity entidadeAtualizada = AluguelEntity.builder().id(id).box(box).cliente(cliente).valor(BigDecimal.valueOf(200.00)).status(false).build();
        when(aluguelRepository.save(any(AluguelEntity.class))).thenReturn(entidadeAtualizada);

        // chama a funcao update
        AluguelResponseDTO result = aluguelService.update(aluguel, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getBox()).isEqualTo(box);
        assertThat(result.getCliente()).isEqualTo(cliente);
        assertThat(result.getValor()).isEqualTo(BigDecimal.valueOf(200.00));
        assertThat(result.getStatus()).isFalse();

        // verifica se buscou o aluguel, o box e o cliente antes de atualizar
        verify(aluguelRepository).findByIdAndDeletedAtIsNull(id);
        verify(boxRepository).findByIdAndDeletedAtIsNull(idBox);
        verify(clienteRepository).findByIdAndDeletedAtIsNull(idCliente);

        // verifica se salvou a entidade com os dados atualizados
        verify(aluguelRepository).save(argThat(e -> e.getId().equals(id) && e.getBox().equals(box) && e.getCliente().equals(cliente)));
    }

    @Test
    @DisplayName("Should throw exception when updating an aluguel that does not exist")
    void updateCase2() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(UUID.randomUUID()).idCliente(UUID.randomUUID()).build();

        // Mock para simular que o aluguel nao existe
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> aluguelService.update(aluguel, id));

        // verifica se buscou o aluguel antes de tentar atualizar
        verify(aluguelRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se nunca chegou a buscar o box, o cliente nem a salvar
        verify(boxRepository, never()).findByIdAndDeletedAtIsNull(any(UUID.class));
        verify(clienteRepository, never()).findByIdAndDeletedAtIsNull(any(UUID.class));
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating aluguel with a box that does not exist")
    void updateCase3() {
        // ids do aluguel e do box utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).build();

        // Mock para simular que o aluguel existe
        BoxEntity boxAntigo = BoxEntity.builder().numero("100").build();
        ClienteEntity clienteAntigo = ClienteEntity.builder().nome("Cliente Old").build();
        AluguelEntity entidadeExistente = AluguelEntity.builder().id(id).box(boxAntigo).cliente(clienteAntigo).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o box nao existe
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> aluguelService.update(aluguel, id));

        // verifica se buscou o aluguel e o box antes de tentar atualizar
        verify(aluguelRepository).findByIdAndDeletedAtIsNull(id);
        verify(boxRepository).findByIdAndDeletedAtIsNull(idBox);

        // verifica se nunca chegou a buscar o cliente nem a salvar, ja que o box nao existe
        verify(clienteRepository, never()).findByIdAndDeletedAtIsNull(any(UUID.class));
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating aluguel with a cliente that does not exist")
    void updateCase4() {
        // ids do aluguel, do box e do cliente utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).build();

        // Mock para simular que o aluguel existe
        BoxEntity boxAntigo = BoxEntity.builder().numero("100").build();
        ClienteEntity clienteAntigo = ClienteEntity.builder().nome("Cliente Old").build();
        AluguelEntity entidadeExistente = AluguelEntity.builder().id(id).box(boxAntigo).cliente(clienteAntigo).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o box existe, mas o cliente nao
        BoxEntity box = BoxEntity.builder().id(idBox).numero("101").build();
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> aluguelService.update(aluguel, id));

        // verifica se buscou o aluguel, o box e o cliente antes de tentar atualizar
        verify(aluguelRepository).findByIdAndDeletedAtIsNull(id);
        verify(boxRepository).findByIdAndDeletedAtIsNull(idBox);
        verify(clienteRepository).findByIdAndDeletedAtIsNull(idCliente);

        // verifica se nunca chegou a salvar, ja que o cliente nao existe
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete aluguel by id")
    void deleteCase1() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o aluguel existe
        AluguelEntity entidadeExistente = AluguelEntity.builder().id(id).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // chama a funcao delete
        aluguelService.delete(id);

        // verifica se buscou o aluguel antes de excluir
        verify(aluguelRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se salvou a entidade com o deletedAt preenchido
        verify(aluguelRepository).save(argThat(e -> e.getId().equals(id) && e.getDeletedAt() != null));
    }

    @Test
    @DisplayName("Should throw exception when deleting an aluguel that does not exist")
    void deleteCase2() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o aluguel nao existe
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao delete e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> aluguelService.delete(id));

        // verifica se nunca chegou a salvar, ja que o aluguel nao existe
        verify(aluguelRepository, never()).save(any());
    }
}
