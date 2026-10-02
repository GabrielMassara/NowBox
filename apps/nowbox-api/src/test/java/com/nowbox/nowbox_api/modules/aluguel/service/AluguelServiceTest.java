package com.nowbox.nowbox_api.modules.aluguel.service;

import com.nowbox.nowbox_api.common.exception.ConflitoException;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelCreateDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelFilterDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelResponseDTO;
import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.repository.IClienteRepository;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import com.nowbox.nowbox_api.modules.contrato.messaging.ContratoSolicitadoMessage;
import com.nowbox.nowbox_api.modules.email.messaging.EmailSolicitadoMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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

    @Mock
    private com.nowbox.nowbox_api.modules.contrato.repository.IArquivoAluguelRepository arquivoAluguelRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AluguelService aluguelService;

    // O box sempre pertence a uma unidade, que e usada na solicitacao do contrato
    private static BoxEntity.BoxEntityBuilder boxBuilder() {
        return BoxEntity.builder().unidade(UnidadeEntity.builder().id(UUID.randomUUID()).cnpj("11111111111111").build());
    }

    @Test
    @DisplayName("Should list one aluguel filtered by idBox, idCliente and status")
    void listAllByFilterCase1() {
        // ids utilizados no filtro
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();

        // inicializa o filtro
        AluguelFilterDTO filtro = AluguelFilterDTO.builder().idBox(idBox).idCliente(idCliente).status(StatusAluguel.ATIVO).build();

        // Mock para simular resposta do Repository
        AluguelEntity entidade = AluguelEntity.builder().box(box).cliente(cliente).valor(BigDecimal.valueOf(150.00)).status(StatusAluguel.ATIVO).build();
        Page<AluguelEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(aluguelRepository.findAllByFilter(idBox, idCliente, StatusAluguel.ATIVO, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<AluguelResponseDTO> result = aluguelService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getBox()).isEqualTo(box);
        assertThat(result.getContent().getFirst().getCliente()).isEqualTo(cliente);
        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(StatusAluguel.ATIVO);

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(aluguelRepository).findAllByFilter(idBox, idCliente, StatusAluguel.ATIVO, null);
    }

    @Test
    @DisplayName("Should list one aluguel filtered by idBox")
    void listAllByFilterCase2() {
        // inicializa o filtro
        UUID idBox = UUID.randomUUID();
        AluguelFilterDTO filtro = AluguelFilterDTO.builder().idBox(idBox).build();

        // Mock para simular resposta do Repository
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
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
        BoxEntity box = boxBuilder().numero("101").build();
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
        BoxEntity box = boxBuilder().numero("101").build();
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
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();

        // dados para criacao
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder()
                .idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(150.00)).observacao("Observação teste").build();

        // Mock para simular que o box e o cliente existem
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        AluguelEntity entidadeSalva = AluguelEntity.builder()
                .id(id).box(box).cliente(cliente).valor(BigDecimal.valueOf(150.00))
                .observacao("Observação teste").status(StatusAluguel.PENDENTE_ASSINATURA_CONTRATO).createdAt(LocalDateTime.now()).build();
        when(aluguelRepository.save(any(AluguelEntity.class))).thenReturn(entidadeSalva);

        // chama a funcao create
        AluguelResponseDTO result = aluguelService.create(aluguel);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getBox()).isEqualTo(box);
        assertThat(result.getCliente()).isEqualTo(cliente);
        assertThat(result.getValor()).isEqualTo(BigDecimal.valueOf(150.00));
        assertThat(result.getObservacao()).isEqualTo("Observação teste");
        assertThat(result.getStatus()).isEqualTo(StatusAluguel.PENDENTE_ASSINATURA_CONTRATO);

        // verifica se buscou o box e o cliente antes de criar
        verify(boxRepository).findByIdAndDeletedAtIsNull(idBox);
        verify(clienteRepository).findByIdAndDeletedAtIsNull(idCliente);

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(aluguelRepository).save(argThat(e -> e.getBox().equals(box) && e.getCliente().equals(cliente) && e.getValor().equals(BigDecimal.valueOf(150.00)) && e.getStatus() == StatusAluguel.PENDENTE_ASSINATURA_CONTRATO));

        // verifica se solicitou a geracao do contrato apenas publicando o evento, sem esperar o contrato ficar pronto
        verify(eventPublisher).publishEvent(argThat((Object e) -> e instanceof ContratoSolicitadoMessage m && m.tipo() == ContratoSolicitadoMessage.Tipo.CONTRATO && m.idAluguel().equals(id) && m.idBox().equals(idBox) && m.numeroBox().equals("101")));
    }

    @Test
    @DisplayName("Should not request the aluguel registered email on creation, only when the signed contract activates the aluguel")
    void createEmailCase1() {
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Maria").email("maria@email.com").build();

        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(new BigDecimal("1234.50")).build();

        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        aluguelService.create(aluguel);

        verify(eventPublisher, never()).publishEvent(argThat((Object e) -> e instanceof EmailSolicitadoMessage));
    }

    @Test
    @DisplayName("Should not request any email when creating an aluguel for a cliente without email")
    void createEmailCase2() {
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Maria").email("  ").build();

        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.TEN).build();

        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenReturn(AluguelEntity.builder()
                .id(UUID.randomUUID()).box(box).cliente(cliente).valor(BigDecimal.TEN).status(StatusAluguel.ATIVO).build());

        aluguelService.create(aluguel);

        verify(eventPublisher, never()).publishEvent(argThat((Object e) -> e instanceof EmailSolicitadoMessage));
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
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();

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
    @DisplayName("Should throw exception when creating an active aluguel for a box that already has an active one")
    void createCase4() {
        // ids do box e do cliente utilizados na criacao
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();

        // dados para criacao de um aluguel ativo
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(150.00)).build();

        // Mock para simular que o box e o cliente existem e que o box ja tem um aluguel ativo
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.existsByBoxIdAndStatusNotAndDeletedAtIsNull(idBox, StatusAluguel.INATIVO)).thenReturn(true);

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(ConflitoException.class, () -> aluguelService.create(aluguel));

        // verifica se nunca chegou a salvar, ja que o box tem um aluguel ativo
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should always create the aluguel pending the contract signature")
    void createCase5() {
        // ids do box e do cliente utilizados na criacao
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();

        // dados para criacao, sem situacao, ja que ela nao e informada pelo usuario
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(150.00)).build();

        // Mock para simular que o box e o cliente existem
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // chama a funcao create
        AluguelResponseDTO result = aluguelService.create(aluguel);

        assertThat(result.getStatus()).isEqualTo(StatusAluguel.PENDENTE_ASSINATURA_CONTRATO);

        // verifica se checou os alugueis em andamento do box e salvou o aluguel pendente de assinatura
        verify(aluguelRepository).existsByBoxIdAndStatusNotAndDeletedAtIsNull(idBox, StatusAluguel.INATIVO);
        verify(aluguelRepository).save(argThat(e -> e.getStatus() == StatusAluguel.PENDENTE_ASSINATURA_CONTRATO));
    }

    @Test
    @DisplayName("Should throw exception when creating an active aluguel for a blocked box")
    void createCase6() {
        // ids do box e do cliente utilizados na criacao
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).numero("101").disponivel(false).build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();

        // dados para criacao de um aluguel ativo
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(150.00)).build();

        // Mock para simular que o box esta bloqueado e o cliente existe
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(ConflitoException.class, () -> aluguelService.create(aluguel));

        // verifica se nunca chegou a salvar, ja que o box esta bloqueado
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
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(200.00)).build();

        // Mock para simular que o aluguel existe
        BoxEntity boxAntigo = boxBuilder().numero("100").build();
        ClienteEntity clienteAntigo = ClienteEntity.builder().nome("Cliente Old").build();
        AluguelEntity entidadeExistente = AluguelEntity.builder().id(id).box(boxAntigo).cliente(clienteAntigo).valor(BigDecimal.valueOf(150.00)).status(StatusAluguel.ATIVO).createdAt(LocalDateTime.now()).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o box e o cliente existem
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));

        // Mock para simular resposta do save
        AluguelEntity entidadeAtualizada = AluguelEntity.builder().id(id).box(box).cliente(cliente).valor(BigDecimal.valueOf(200.00)).status(StatusAluguel.ATIVO).build();
        when(aluguelRepository.save(any(AluguelEntity.class))).thenReturn(entidadeAtualizada);

        // chama a funcao update
        AluguelResponseDTO result = aluguelService.update(aluguel, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getBox()).isEqualTo(box);
        assertThat(result.getCliente()).isEqualTo(cliente);
        assertThat(result.getValor()).isEqualTo(BigDecimal.valueOf(200.00));
        assertThat(result.getStatus()).isEqualTo(StatusAluguel.ATIVO);

        // verifica se buscou o aluguel, o box e o cliente antes de atualizar
        verify(aluguelRepository).findByIdAndDeletedAtIsNull(id);
        verify(boxRepository).findByIdAndDeletedAtIsNull(idBox);
        verify(clienteRepository).findByIdAndDeletedAtIsNull(idCliente);

        // verifica se salvou a entidade com os dados atualizados
        verify(aluguelRepository).save(argThat(e -> e.getId().equals(id) && e.getBox().equals(box) && e.getCliente().equals(cliente)));

        // verifica se solicitou um aditivo com as alteracoes, ja que o contrato original e mantido
        verify(eventPublisher).publishEvent(argThat((Object e) -> e instanceof ContratoSolicitadoMessage m && m.tipo() == ContratoSolicitadoMessage.Tipo.ADITIVO && m.idAluguel().equals(id)
                && m.alteracoes().stream().anyMatch(a -> a.campo().equals("Box") && a.valorAnterior().equals("100") && a.valorNovo().equals("101"))));
    }

    @Test
    @DisplayName("Should keep the original contrato of the aluguel when updating it")
    void updateContratoOriginal() {
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();
        ArquivoEntity original = ArquivoEntity.builder().id(UUID.randomUUID()).chave("contratos/original.pdf").build();

        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(200.00)).build();

        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(AluguelEntity.builder().id(id).box(box).cliente(cliente)
                .valor(BigDecimal.valueOf(150.00)).status(StatusAluguel.ATIVO).contrato(original).createdAt(LocalDateTime.now()).build()));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(i -> i.getArgument(0));

        aluguelService.update(aluguel, id);

        verify(aluguelRepository).save(argThat(e -> e.getContrato() == original));
        verify(eventPublisher).publishEvent(argThat((Object e) -> e instanceof ContratoSolicitadoMessage m && m.tipo() == ContratoSolicitadoMessage.Tipo.ADITIVO
                && m.alteracoes().size() == 1 && m.alteracoes().getFirst().campo().equals("Valor")));
    }

    @Test
    @DisplayName("Should not request an aditivo nor the changed email when updating an aluguel without changes")
    void updateSemAlteracoes() {
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Maria").email("maria@email.com").build();

        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(new BigDecimal("150.00")).observacao("  ").build();

        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(AluguelEntity.builder().id(id).box(box).cliente(cliente)
                .valor(new BigDecimal("150.0")).status(StatusAluguel.ATIVO).createdAt(LocalDateTime.now()).build()));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(i -> i.getArgument(0));

        aluguelService.update(aluguel, id);

        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Should throw exception when the box has another active aluguel")
    void updateCase5() {
        // ids do aluguel, do box e do cliente utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        // dados para atualizacao com o aluguel ativo
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(200.00)).build();

        // Mock para simular que o aluguel, o box e o cliente existem
        AluguelEntity entidadeExistente = AluguelEntity.builder().id(id).status(StatusAluguel.ATIVO).createdAt(LocalDateTime.now()).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(boxBuilder().id(idBox).build()));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(ClienteEntity.builder().id(idCliente).build()));

        // Mock para simular que o box ja tem outro aluguel ativo
        when(aluguelRepository.existsByBoxIdAndStatusNotAndDeletedAtIsNullAndIdNot(idBox, StatusAluguel.INATIVO, id)).thenReturn(true);

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(ConflitoException.class, () -> aluguelService.update(aluguel, id));

        // verifica se nunca chegou a salvar, ja que o box tem outro aluguel ativo
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should request the aluguel changed email when updating an aluguel for a cliente with email")
    void updateEmailCase1() {
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = BoxEntity.builder().id(idBox).numero("101")
                .unidade(UnidadeEntity.builder().id(UUID.randomUUID()).nome("Unidade Centro").cnpj("11111111111111").build()).build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Maria").email(" maria@email.com ").build();

        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(new BigDecimal("1234.50")).build();

        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(AluguelEntity.builder().id(id).box(box).cliente(cliente).status(StatusAluguel.ATIVO).createdAt(LocalDateTime.now()).build()));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenReturn(AluguelEntity.builder()
                .id(id).box(box).cliente(cliente).valor(new BigDecimal("1234.50")).status(StatusAluguel.ATIVO).build());

        aluguelService.update(aluguel, id);

        verify(eventPublisher).publishEvent(argThat((Object e) -> e instanceof EmailSolicitadoMessage m
                && m.template() == EmailSolicitadoMessage.Template.ALUGUEL_ALTERADO
                && m.destinatario().equals("maria@email.com")
                && m.nomeDestinatario().equals("Maria")
                && m.variaveis().get("numeroBox").equals("101")
                && m.variaveis().get("unidade").equals("Unidade Centro")
                && m.variaveis().get("situacao").equals("Ativo")
                && m.variaveis().get("valor").replace(' ', ' ').equals("R$ 1.234,50")));
    }

    @Test
    @DisplayName("Should not request the aluguel changed email when the cliente has no email")
    void updateEmailCase2() {
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Maria").email(null).build();

        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.TEN).build();

        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(AluguelEntity.builder().id(id).box(box).cliente(cliente).status(StatusAluguel.ATIVO).createdAt(LocalDateTime.now()).build()));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenReturn(AluguelEntity.builder()
                .id(id).box(box).cliente(cliente).valor(BigDecimal.TEN).status(StatusAluguel.ATIVO).build());

        aluguelService.update(aluguel, id);

        verify(eventPublisher, never()).publishEvent(argThat((Object e) -> e instanceof EmailSolicitadoMessage));
    }

    @Test
    @DisplayName("Should update an active aluguel, leaving it pending the aditivo, when no other active one exists for the box")
    void updateCase6() {
        // ids do aluguel, do box e do cliente utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        // dados para atualizacao mantendo o aluguel ativo
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(200.00)).build();

        // Mock para simular que o aluguel, o box e o cliente existem
        AluguelEntity entidadeExistente = AluguelEntity.builder().id(id).status(StatusAluguel.ATIVO).createdAt(LocalDateTime.now()).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(boxBuilder().id(idBox).build()));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(ClienteEntity.builder().id(idCliente).build()));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // chama a funcao update
        AluguelResponseDTO result = aluguelService.update(aluguel, id);

        assertThat(result.getStatus()).isEqualTo(StatusAluguel.PENDENTE_ASSINATURA_ADITIVO);

        // verifica se procurou outro aluguel ativo desconsiderando o proprio aluguel
        verify(aluguelRepository).existsByBoxIdAndStatusNotAndDeletedAtIsNullAndIdNot(idBox, StatusAluguel.INATIVO, id);
    }

    @Test
    @DisplayName("Should throw exception when moving an active aluguel to a blocked box")
    void updateCase7() {
        // ids do aluguel, do box e do cliente utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        // dados para atualizacao mantendo o aluguel ativo em outro box
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(200.00)).build();

        // Mock para simular que o aluguel esta ativo em outro box e que o novo box esta bloqueado
        BoxEntity boxAntigo = boxBuilder().id(UUID.randomUUID()).build();
        AluguelEntity entidadeExistente = AluguelEntity.builder().id(id).box(boxAntigo).status(StatusAluguel.ATIVO).createdAt(LocalDateTime.now()).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(boxBuilder().id(idBox).disponivel(false).build()));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(ClienteEntity.builder().id(idCliente).build()));

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(ConflitoException.class, () -> aluguelService.update(aluguel, id));

        // verifica se nunca chegou a salvar, ja que o box esta bloqueado
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating an aluguel that was already ended")
    void updateCase8() {
        // ids do aluguel, do box e do cliente utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        // dados para atualizacao de um aluguel encerrado
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(200.00)).build();

        // Mock para simular que o aluguel ja foi encerrado
        BoxEntity boxBloqueado = boxBuilder().id(idBox).disponivel(false).build();
        AluguelEntity entidadeExistente = AluguelEntity.builder().id(id).box(boxBloqueado).status(StatusAluguel.INATIVO).createdAt(LocalDateTime.now()).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(boxBloqueado));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(ClienteEntity.builder().id(idCliente).build()));

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(ConflitoException.class, () -> aluguelService.update(aluguel, id));

        // verifica se nunca chegou a salvar, ja que um contrato encerrado nao pode ser alterado nem reativado
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update an aluguel that is already active in a box that was blocked afterwards")
    void updateCase9() {
        // ids do aluguel, do box e do cliente utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        // dados para atualizacao mantendo o aluguel ativo no mesmo box
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(220.00)).build();

        // Mock para simular que o aluguel ja esta ativo no box, que foi bloqueado depois
        BoxEntity boxBloqueado = boxBuilder().id(idBox).disponivel(false).build();
        AluguelEntity entidadeExistente = AluguelEntity.builder().id(id).box(boxBloqueado).status(StatusAluguel.ATIVO).createdAt(LocalDateTime.now()).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(boxBloqueado));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(ClienteEntity.builder().id(idCliente).build()));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // chama a funcao update
        AluguelResponseDTO result = aluguelService.update(aluguel, id);

        assertThat(result.getValor()).isEqualTo(BigDecimal.valueOf(220.00));
        verify(aluguelRepository).save(any(AluguelEntity.class));
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
        BoxEntity boxAntigo = boxBuilder().numero("100").build();
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
        BoxEntity boxAntigo = boxBuilder().numero("100").build();
        ClienteEntity clienteAntigo = ClienteEntity.builder().nome("Cliente Old").build();
        AluguelEntity entidadeExistente = AluguelEntity.builder().id(id).box(boxAntigo).cliente(clienteAntigo).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o box existe, mas o cliente nao
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
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

    @Test
    @DisplayName("Should start the ending: pending the distrato signature and request the distrato")
    void encerrarCase1() {
        UUID id = UUID.randomUUID();
        BoxEntity box = BoxEntity.builder().id(UUID.randomUUID()).numero("101")
                .unidade(UnidadeEntity.builder().id(UUID.randomUUID()).nome("Unidade Centro").cnpj("11111111111111").build()).build();
        ClienteEntity cliente = ClienteEntity.builder().id(UUID.randomUUID()).nome("Maria").email("maria@email.com").build();
        AluguelEntity ativo = AluguelEntity.builder().id(id).box(box).cliente(cliente).valor(BigDecimal.TEN).status(StatusAluguel.ATIVO).createdAt(LocalDateTime.now()).build();

        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(ativo));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AluguelResponseDTO result = aluguelService.encerrar(id);

        assertThat(result.getStatus()).isEqualTo(StatusAluguel.PENDENTE_ASSINATURA_DISTRATO);
        verify(aluguelRepository).save(argThat(e -> e.getId().equals(id) && e.getStatus() == StatusAluguel.PENDENTE_ASSINATURA_DISTRATO));

        // gera um distrato e nao um aditivo
        verify(eventPublisher).publishEvent(argThat((Object e) -> e instanceof ContratoSolicitadoMessage m
                && m.tipo() == ContratoSolicitadoMessage.Tipo.DISTRATO && m.idAluguel().equals(id)));
        verify(eventPublisher, never()).publishEvent(argThat((Object e) -> e instanceof ContratoSolicitadoMessage m && m.tipo() == ContratoSolicitadoMessage.Tipo.ADITIVO));
        // o email de encerramento so sai quando o distrato assinado for enviado
        verify(eventPublisher, never()).publishEvent(argThat((Object e) -> e instanceof EmailSolicitadoMessage));
    }

    @Test
    @DisplayName("Should not request any email when starting the ending of an aluguel")
    void encerrarCase2() {
        UUID id = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(UUID.randomUUID()).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(UUID.randomUUID()).nome("Maria").email(null).build();

        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(AluguelEntity.builder().id(id).box(box).cliente(cliente).status(StatusAluguel.ATIVO).build()));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        aluguelService.encerrar(id);

        verify(eventPublisher).publishEvent(argThat((Object e) -> e instanceof ContratoSolicitadoMessage m && m.tipo() == ContratoSolicitadoMessage.Tipo.DISTRATO));
        verify(eventPublisher, never()).publishEvent(argThat((Object e) -> e instanceof EmailSolicitadoMessage));
    }

    @Test
    @DisplayName("Should throw exception when ending an aluguel that is still pending the contract signature")
    void encerrarPendente() {
        UUID id = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(AluguelEntity.builder().id(id).status(StatusAluguel.PENDENTE_ASSINATURA_CONTRATO).build()));

        assertThrows(ConflitoException.class, () -> aluguelService.encerrar(id));

        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating an aluguel that is not active")
    void updateNaoAtivo() {
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();

        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(AluguelEntity.builder().id(id).box(box).cliente(cliente).status(StatusAluguel.PENDENTE_ASSINATURA_CONTRATO).build()));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));

        assertThrows(ConflitoException.class, () -> aluguelService.update(AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.TEN).build(), id));

        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when ending an aluguel that was already ended")
    void encerrarCase3() {
        UUID id = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(AluguelEntity.builder().id(id).status(StatusAluguel.INATIVO).build()));

        assertThrows(ConflitoException.class, () -> aluguelService.encerrar(id));

        verify(aluguelRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Should throw exception when ending an aluguel that does not exist")
    void encerrarCase4() {
        UUID id = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        assertThrows(NaoEncontradoException.class, () -> aluguelService.encerrar(id));

        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should leave the aluguel pending the aditivo signature when updating it")
    void updateStatusMantido() {
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();

        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(AluguelEntity.builder().id(id).box(box).cliente(cliente).valor(BigDecimal.TEN).status(StatusAluguel.ATIVO).createdAt(LocalDateTime.now()).build()));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        aluguelService.update(AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(20)).build(), id);

        // a situacao nao e alterada pela edicao e nao entra na lista de alteracoes do aditivo
        verify(aluguelRepository).save(argThat(e -> e.getStatus() == StatusAluguel.PENDENTE_ASSINATURA_ADITIVO));
        verify(eventPublisher).publishEvent(argThat((Object e) -> e instanceof ContratoSolicitadoMessage m
                && m.tipo() == ContratoSolicitadoMessage.Tipo.ADITIVO && m.alteracoes().stream().noneMatch(a -> a.campo().equals("Situação"))));
    }

    @Test
    @DisplayName("Should make the aluguel inactive when cancelling the pending contract signature")
    void cancelarPendenciaContrato() {
        UUID id = UUID.randomUUID();
        AluguelEntity aluguel = AluguelEntity.builder().id(id).status(StatusAluguel.PENDENTE_ASSINATURA_CONTRATO).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguel));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AluguelResponseDTO result = aluguelService.cancelarPendencia(id);

        assertThat(result.getStatus()).isEqualTo(StatusAluguel.INATIVO);
    }

    @Test
    @DisplayName("Should keep the aluguel active and discard the distrato when cancelling the pending distrato signature")
    void cancelarPendenciaDistrato() {
        UUID id = UUID.randomUUID();
        AluguelEntity aluguel = AluguelEntity.builder().id(id).status(StatusAluguel.PENDENTE_ASSINATURA_DISTRATO)
                .distrato(com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity.builder().id(UUID.randomUUID()).build()).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguel));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AluguelResponseDTO result = aluguelService.cancelarPendencia(id);

        assertThat(result.getStatus()).isEqualTo(StatusAluguel.ATIVO);
        assertThat(result.isDistratoGerado()).isFalse();
        assertThat(aluguel.getDistrato()).isNull();
    }

    @Test
    @DisplayName("Should restore the original values when cancelling the pending aditivo signature")
    void cancelarPendenciaAditivo() {
        UUID id = UUID.randomUUID();
        BoxEntity boxOriginal = boxBuilder().id(UUID.randomUUID()).numero("101").build();
        BoxEntity boxNovo = boxBuilder().id(UUID.randomUUID()).numero("202").build();
        ClienteEntity cliente = ClienteEntity.builder().id(UUID.randomUUID()).nome("Cliente").build();
        com.nowbox.nowbox_api.modules.contrato.entity.ArquivoAluguelEntity aditivo = com.nowbox.nowbox_api.modules.contrato.entity.ArquivoAluguelEntity.builder().pendenteAssinatura(true).build();
        AluguelEntity aluguel = AluguelEntity.builder().id(id).status(StatusAluguel.PENDENTE_ASSINATURA_ADITIVO)
                .box(boxNovo).cliente(cliente).valor(BigDecimal.valueOf(200)).observacao("nova")
                .boxAnterior(boxOriginal).clienteAnterior(cliente).valorAnterior(BigDecimal.valueOf(100)).observacaoAnterior("antiga").build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguel));
        when(arquivoAluguelRepository.findFirstByAluguelIdAndPendenteAssinaturaTrueOrderBySalvoEmDesc(id)).thenReturn(Optional.of(aditivo));
        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AluguelResponseDTO result = aluguelService.cancelarPendencia(id);

        assertThat(result.getStatus()).isEqualTo(StatusAluguel.ATIVO);
        assertThat(aluguel.getBox()).isEqualTo(boxOriginal);
        assertThat(aluguel.getValor()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(aluguel.getObservacao()).isEqualTo("antiga");
        assertThat(aluguel.getBoxAnterior()).isNull();
        assertThat(aditivo.isCancelado()).isTrue();
        assertThat(aditivo.isPendenteAssinatura()).isFalse();
        verify(arquivoAluguelRepository).save(aditivo);
    }

    @Test
    @DisplayName("Should not rent a box that is reserved by an aluguel pending the aditivo signature that moves out of it")
    void createBoxReservadoPorAditivo() {
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(boxBuilder().id(idBox).build()));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(ClienteEntity.builder().id(idCliente).build()));
        when(aluguelRepository.existsByBoxAnteriorIdAndStatusAndDeletedAtIsNull(idBox, StatusAluguel.PENDENTE_ASSINATURA_ADITIVO)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> aluguelService.create(AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.TEN).build()));

        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not move another aluguel to a box that is reserved by a pending aditivo")
    void updateBoxReservadoPorAditivo() {
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        BoxEntity box = boxBuilder().id(idBox).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(AluguelEntity.builder().id(id).status(StatusAluguel.ATIVO).build()));
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));
        when(clienteRepository.findByIdAndDeletedAtIsNull(idCliente)).thenReturn(Optional.of(ClienteEntity.builder().id(idCliente).build()));
        when(aluguelRepository.existsByBoxAnteriorIdAndStatusAndDeletedAtIsNullAndIdNot(idBox, StatusAluguel.PENDENTE_ASSINATURA_ADITIVO, id)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> aluguelService.update(AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.TEN).build(), id));

        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cancelling an aluguel that is not pending a signature")
    void cancelarPendenciaSemPendencia() {
        UUID id = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(AluguelEntity.builder().id(id).status(StatusAluguel.ATIVO).build()));

        assertThrows(ConflitoException.class, () -> aluguelService.cancelarPendencia(id));

        verify(aluguelRepository, never()).save(any());
    }
}
