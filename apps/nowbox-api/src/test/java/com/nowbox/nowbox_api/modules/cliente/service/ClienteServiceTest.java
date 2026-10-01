package com.nowbox.nowbox_api.modules.cliente.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.common.exception.RequisicaoInvalidaException;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteCreateDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteFilterDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteResponseDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.DocumentoDownloadDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.DocumentoHistoricoDTO;
import com.nowbox.nowbox_api.modules.cliente.entity.ArquivoClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.repository.IArquivoClienteRepository;
import com.nowbox.nowbox_api.modules.cliente.repository.IClienteRepository;
import com.nowbox.nowbox_api.modules.cliente.storage.DocumentoStorageService;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import com.nowbox.nowbox_api.modules.contrato.repository.IArquivoRepository;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.estado.repository.IEstadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private IClienteRepository clienteRepository;

    @Mock
    private IEstadoRepository estadoRepository;

    @Mock
    private IArquivoRepository arquivoRepository;

    @Mock
    private IArquivoClienteRepository arquivoClienteRepository;

    @Mock
    private DocumentoStorageService documentoStorageService;

    // Documento valido (cabecalho de PDF) para os cenarios de cadastro
    private MockMultipartFile documentoPdf() {
        return new MockMultipartFile("documento", "rg.pdf", "application/pdf", "%PDF-1.4 conteudo".getBytes());
    }

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("Should list one cliente filtered by idEstado, nome, cpf and email")
    void listAllByFilterCase1() {
        // id do estado utilizado no filtro
        UUID idEstado = UUID.randomUUID();
        EstadoEntity estado = EstadoEntity.builder().id(idEstado).nome("Estado Test").uf("XX").build();

        // inicializa o filtro
        ClienteFilterDTO filtro = ClienteFilterDTO.builder().idEstado(idEstado).nome("Cliente Test").cpf("12345678901").email("cliente@test.com").build();

        // Mock para simular resposta do Repository
        ClienteEntity entidade = ClienteEntity.builder().nome("Cliente Test").cpf("12345678901").email("cliente@test.com").estado(estado).build();
        Page<ClienteEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(clienteRepository.findAllByFilter(idEstado, "Cliente Test", "12345678901", "cliente@test.com", null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<ClienteResponseDTO> result = clienteService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Cliente Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(clienteRepository).findAllByFilter(idEstado, "Cliente Test", "12345678901", "cliente@test.com", null);
    }

    @Test
    @DisplayName("Should list one cliente filtered by nome")
    void listAllByFilterCase2() {
        // inicializa o filtro
        ClienteFilterDTO filtro = ClienteFilterDTO.builder().nome("Cliente Test").build();

        // Mock para simular resposta do Repository
        EstadoEntity estado = EstadoEntity.builder().nome("Estado Test").uf("XX").build();
        ClienteEntity entidade = ClienteEntity.builder().nome("Cliente Test").estado(estado).build();
        Page<ClienteEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(clienteRepository.findAllByFilter(null, "Cliente Test", null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<ClienteResponseDTO> result = clienteService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNome()).isEqualTo("Cliente Test");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(clienteRepository).findAllByFilter(null, "Cliente Test", null, null, null);
    }

    @Test
    @DisplayName("Should list all clientes when filter is null")
    void listAllByFilterCase3() {
        // Mock para simular resposta do Repository
        EstadoEntity estado = EstadoEntity.builder().nome("Estado Test").uf("XX").build();
        ClienteEntity entidade1 = ClienteEntity.builder().nome("Cliente 1").estado(estado).build();
        ClienteEntity entidade2 = ClienteEntity.builder().nome("Cliente 2").estado(estado).build();
        Page<ClienteEntity> paginaMock = new PageImpl<>(List.of(entidade1, entidade2));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(clienteRepository.findAllByFilter(null, null, null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter passando filtro nulo
        Page<ClienteResponseDTO> result = clienteService.listAllByFilter(null, null);

        assertThat(result.getContent()).hasSize(2);

        // verifica se ao chamar a findAllByFilter ele passou todos os parametros nulos
        verify(clienteRepository).findAllByFilter(null, null, null, null, null);
    }

    @Test
    @DisplayName("Should not list cliente when filter does not match")
    void listAllByFilterCase4() {
        // inicializa o filtro
        ClienteFilterDTO filtro = ClienteFilterDTO.builder().nome("Cliente Inexistente").build();

        // Mock para simular resposta do Repository
        Page<ClienteEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(clienteRepository.findAllByFilter(null, "Cliente Inexistente", null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<ClienteResponseDTO> result = clienteService.listAllByFilter(null, filtro);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(clienteRepository).findAllByFilter(null, "Cliente Inexistente", null, null, null);
    }

    @Test
    @DisplayName("Should return cliente when id exists")
    void findCase1() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Repository
        EstadoEntity estado = EstadoEntity.builder().nome("Estado Test").uf("XX").build();
        ClienteEntity entidade = ClienteEntity.builder().id(id).nome("Cliente Test").estado(estado).build();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna o mock entidade
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidade));

        // chama a funcao find
        ClienteResponseDTO result = clienteService.find(null, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Cliente Test");

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(clienteRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should throw exception when cliente id does not exist")
    void findCase2() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna vazio
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao find e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> clienteService.find(null, id));

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(clienteRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should save and return created cliente")
    void createCase1() {
        // id do estado utilizado na criacao
        UUID idEstado = UUID.randomUUID();
        EstadoEntity estado = EstadoEntity.builder().id(idEstado).nome("Estado Test").uf("XX").build();

        // dados para criacao
        ClienteCreateDTO cliente = ClienteCreateDTO.builder()
                .idEstado(idEstado)
                .nome("Cliente Test")
                .profissao("Engenheiro")
                .cpf("12345678901")
                .rg("123456789")
                .email("cliente@test.com")
                .telefone("11999999999")
                .sexo("M")
                .nascimento(LocalDate.of(1990, 1, 1))
                .endereco("Rua Teste")
                .numero("100")
                .bairro("Bairro Teste")
                .cep("11111111")
                .cidade("Cidade Teste")
                .enderecoCorrespondencia(true)
                .senha("senha123")
                .senhaTemporariaStatus(false)
                .build();

        // Mock para simular que o estado existe
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.of(estado));

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        ClienteEntity entidadeSalva = ClienteEntity.builder()
                .id(id)
                .nome("Cliente Test")
                .cpf("12345678901")
                .email("cliente@test.com")
                .estado(estado)
                .createdAt(LocalDateTime.now())
                .build();
        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(entidadeSalva);

        // Mock para simular o registro do arquivo
        when(arquivoRepository.save(any(ArquivoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // chama a funcao create
        ClienteResponseDTO result = clienteService.create(cliente, documentoPdf());

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Cliente Test");
        assertThat(result.getCpf()).isEqualTo("12345678901");
        assertThat(result.getEstado()).isEqualTo(estado);

        // verifica se buscou o estado antes de criar
        verify(estadoRepository).findById(idEstado);

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(clienteRepository).save(argThat(e -> e.getNome().equals("Cliente Test") && e.getCpf().equals("12345678901") && e.getEstado().equals(estado)
                && e.getDocumentoIdentidade() != null && e.getDocumentoIdentidade().getContentType().equals("application/pdf")));

        // verifica se gravou o documento direto no storage, sem fila
        verify(documentoStorageService).salvar(argThat(c -> c.startsWith("clientes/documentos-identidade/") && c.endsWith(".pdf")), any(), anyLong(), eq("application/pdf"));

        // verifica se o documento entrou no historico do cliente
        verify(arquivoClienteRepository).save(argThat(h -> h.getCliente() == entidadeSalva && h.getArquivo().getContentType().equals("application/pdf") && h.getSalvoEm() != null));
    }

    @Test
    @DisplayName("Should throw exception when creating cliente without documento")
    void createCase3() {
        ClienteCreateDTO cliente = ClienteCreateDTO.builder().idEstado(UUID.randomUUID()).nome("Cliente Test").build();

        // sem arquivo e com arquivo vazio
        assertThrows(RequisicaoInvalidaException.class, () -> clienteService.create(cliente, null));
        assertThrows(RequisicaoInvalidaException.class, () -> clienteService.create(cliente, new MockMultipartFile("documento", "rg.pdf", "application/pdf", new byte[0])));

        // nada deve ser gravado
        verify(documentoStorageService, never()).salvar(anyString(), any(), anyLong(), anyString());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating cliente with documento that is not an image or pdf")
    void createCase4() {
        ClienteCreateDTO cliente = ClienteCreateDTO.builder().idEstado(UUID.randomUUID()).nome("Cliente Test").build();

        // o content-type informado e pdf, mas o conteudo nao e
        MockMultipartFile falso = new MockMultipartFile("documento", "rg.pdf", "application/pdf", "<html>nao e pdf</html>".getBytes());

        assertThrows(RequisicaoInvalidaException.class, () -> clienteService.create(cliente, falso));

        verify(documentoStorageService, never()).salvar(anyString(), any(), anyLong(), anyString());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating cliente with an estado that does not exist")
    void createCase2() {
        // id do estado inexistente
        UUID idEstado = UUID.randomUUID();

        // dados para criacao
        ClienteCreateDTO cliente = ClienteCreateDTO.builder().idEstado(idEstado).nome("Cliente Test").build();

        // Mock para simular que o estado nao existe
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> clienteService.create(cliente, documentoPdf()));

        // verifica se buscou o estado antes de tentar criar
        verify(estadoRepository).findById(idEstado);

        // verifica se nunca chegou a salvar, ja que o estado nao existe
        verify(clienteRepository, never()).save(any());
        verify(documentoStorageService, never()).salvar(anyString(), any(), anyLong(), anyString());
    }

    @Test
    @DisplayName("Should update cliente when id and estado exist")
    void updateCase1() {
        // id do cliente e do estado utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idEstado = UUID.randomUUID();

        // dados para atualizacao
        ClienteCreateDTO cliente = ClienteCreateDTO.builder().idEstado(idEstado).nome("Cliente Atualizado").enderecoCorrespondencia(false).build();

        // Mock para simular que o cliente existe
        EstadoEntity estadoAntigo = EstadoEntity.builder().nome("Estado Old").uf("YY").build();
        ArquivoEntity documentoAntigo = ArquivoEntity.builder().id(UUID.randomUUID()).chave("clientes/documentos-identidade/antigo.pdf").build();
        ClienteEntity entidadeExistente = ClienteEntity.builder().id(id).nome("Cliente Test").enderecoCorrespondencia(true).estado(estadoAntigo).documentoIdentidade(documentoAntigo).createdAt(LocalDateTime.now()).build();
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));
        when(arquivoRepository.save(any(ArquivoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Mock para simular que o estado existe
        EstadoEntity estado = EstadoEntity.builder().id(idEstado).nome("Estado Test").uf("XX").build();
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.of(estado));

        // Mock para simular resposta do save
        ClienteEntity entidadeAtualizada = ClienteEntity.builder().id(id).nome("Cliente Atualizado").enderecoCorrespondencia(false).estado(estado).build();
        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(entidadeAtualizada);

        // chama a funcao update
        ClienteResponseDTO result = clienteService.update(cliente, documentoPdf(), id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNome()).isEqualTo("Cliente Atualizado");
        assertThat(result.getEnderecoCorrespondencia()).isFalse();
        assertThat(result.getEstado()).isEqualTo(estado);

        // verifica se buscou o cliente e o estado antes de atualizar
        verify(clienteRepository).findByIdAndDeletedAtIsNull(id);
        verify(estadoRepository).findById(idEstado);

        // verifica se salvou a entidade com os dados atualizados
        verify(clienteRepository).save(argThat(e -> e.getId().equals(id) && e.getNome().equals("Cliente Atualizado") && e.getEstado().equals(estado)
                && e.getDocumentoIdentidade() != documentoAntigo));

        // verifica se gravou o novo documento no historico e manteve o antigo registrado e no storage
        verify(documentoStorageService).salvar(anyString(), any(), anyLong(), eq("application/pdf"));
        verify(arquivoClienteRepository).save(argThat(h -> h.getCliente() == entidadeAtualizada && h.getArquivo() != documentoAntigo));
        verify(arquivoRepository, never()).delete(any());
        verify(documentoStorageService, never()).remover(anyString());
    }

    @Test
    @DisplayName("Should keep the current documento when updating cliente without a new one")
    void updateCase4() {
        UUID id = UUID.randomUUID();
        UUID idEstado = UUID.randomUUID();
        ClienteCreateDTO cliente = ClienteCreateDTO.builder().idEstado(idEstado).nome("Cliente Atualizado").build();

        EstadoEntity estado = EstadoEntity.builder().id(idEstado).nome("Estado Test").uf("XX").build();
        ArquivoEntity documentoAtual = ArquivoEntity.builder().id(UUID.randomUUID()).chave("clientes/documentos-identidade/atual.pdf").nomeOriginal("rg.pdf").contentType("application/pdf").tamanho(10L).build();
        ClienteEntity entidadeExistente = ClienteEntity.builder().id(id).nome("Cliente Test").estado(estado).documentoIdentidade(documentoAtual).build();
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.of(estado));
        when(clienteRepository.save(any(ClienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponseDTO result = clienteService.update(cliente, null, id);

        assertThat(result.getDocumentoIdentidade().id()).isEqualTo(documentoAtual.getId());

        // nada e gravado nem apagado no storage
        verify(clienteRepository).save(argThat(e -> e.getDocumentoIdentidade() == documentoAtual));
        verify(documentoStorageService, never()).salvar(anyString(), any(), anyLong(), anyString());
        verify(documentoStorageService, never()).remover(anyString());
        verify(arquivoRepository, never()).delete(any());
        verify(arquivoClienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating a cliente that does not exist")
    void updateCase2() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        ClienteCreateDTO cliente = ClienteCreateDTO.builder().idEstado(UUID.randomUUID()).nome("Cliente Test").build();

        // Mock para simular que o cliente nao existe
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> clienteService.update(cliente, null, id));

        // verifica se buscou o cliente antes de tentar atualizar
        verify(clienteRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se nunca chegou a buscar o estado nem a salvar
        verify(estadoRepository, never()).findById(any(UUID.class));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating cliente with an estado that does not exist")
    void updateCase3() {
        // id do cliente e do estado utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idEstado = UUID.randomUUID();
        ClienteCreateDTO cliente = ClienteCreateDTO.builder().idEstado(idEstado).nome("Cliente Test").build();

        // Mock para simular que o cliente existe
        EstadoEntity estadoAntigo = EstadoEntity.builder().nome("Estado Old").uf("YY").build();
        ClienteEntity entidadeExistente = ClienteEntity.builder().id(id).nome("Cliente Test").estado(estadoAntigo).build();
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que o estado nao existe
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> clienteService.update(cliente, null, id));

        // verifica se buscou o cliente e o estado antes de tentar atualizar
        verify(clienteRepository).findByIdAndDeletedAtIsNull(id);
        verify(estadoRepository).findById(idEstado);

        // verifica se nunca chegou a salvar, ja que o estado nao existe
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete cliente by id")
    void deleteCase1() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o cliente existe
        ClienteEntity entidadeExistente = ClienteEntity.builder().id(id).nome("Cliente Test").build();
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // chama a funcao delete
        clienteService.delete(id);

        // verifica se buscou o cliente antes de excluir
        verify(clienteRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se salvou a entidade com o deletedAt preenchido
        verify(clienteRepository).save(argThat(e -> e.getId().equals(id) && e.getDeletedAt() != null));
    }

    @Test
    @DisplayName("Should throw exception when deleting a cliente that does not exist")
    void deleteCase2() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o cliente nao existe
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao delete e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> clienteService.delete(id));

        // verifica se nunca chegou a salvar, ja que o cliente nao existe
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should list the documentos history of a cliente marking the current one")
    void listDocumentosCase1() {
        UUID id = UUID.randomUUID();
        ArquivoEntity atual = ArquivoEntity.builder().id(UUID.randomUUID()).nomeOriginal("novo.pdf").contentType("application/pdf").tamanho(20L).build();
        ArquivoEntity antigo = ArquivoEntity.builder().id(UUID.randomUUID()).nomeOriginal("antigo.pdf").contentType("application/pdf").tamanho(10L).build();
        ClienteEntity cliente = ClienteEntity.builder().id(id).documentoIdentidade(atual).build();
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(cliente));

        LocalDateTime agora = LocalDateTime.now();
        PageRequest pageable = PageRequest.of(0, 10);
        when(arquivoClienteRepository.findByClienteIdOrderBySalvoEmDesc(id, pageable)).thenReturn(new PageImpl<>(List.of(
                ArquivoClienteEntity.builder().id(UUID.randomUUID()).arquivo(atual).cliente(cliente).salvoEm(agora).build(),
                ArquivoClienteEntity.builder().id(UUID.randomUUID()).arquivo(antigo).cliente(cliente).salvoEm(agora.minusDays(1)).build())));

        Page<DocumentoHistoricoDTO> result = clienteService.listDocumentos(pageable, id);

        assertThat(result.getContent()).extracting(DocumentoHistoricoDTO::nomeArquivo).containsExactly("novo.pdf", "antigo.pdf");
        assertThat(result.getContent()).extracting(DocumentoHistoricoDTO::atual).containsExactly(true, false);
    }

    @Test
    @DisplayName("Should throw exception when listing documentos of a cliente that does not exist")
    void listDocumentosCase2() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        assertThrows(NaoEncontradoException.class, () -> clienteService.listDocumentos(PageRequest.of(0, 10), id));

        verify(arquivoClienteRepository, never()).findByClienteIdOrderBySalvoEmDesc(any(), any());
    }

    @Test
    @DisplayName("Should download a documento of the history")
    void downloadHistoricoCase1() {
        UUID id = UUID.randomUUID();
        UUID idDocumento = UUID.randomUUID();
        ArquivoEntity arquivo = ArquivoEntity.builder().id(UUID.randomUUID()).chave("clientes/documentos-identidade/antigo.pdf").nomeOriginal("antigo.pdf").contentType("application/pdf").tamanho(10L).build();
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(ClienteEntity.builder().id(id).build()));
        when(arquivoClienteRepository.findByIdAndClienteId(idDocumento, id)).thenReturn(Optional.of(ArquivoClienteEntity.builder().id(idDocumento).arquivo(arquivo).build()));
        when(documentoStorageService.abrir("clientes/documentos-identidade/antigo.pdf")).thenReturn(new ByteArrayInputStream(new byte[]{1}));

        DocumentoDownloadDTO result = clienteService.downloadHistorico(id, idDocumento);

        assertThat(result.nomeArquivo()).isEqualTo("antigo.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
    }

    @Test
    @DisplayName("Should throw exception when the documento does not belong to the cliente")
    void downloadHistoricoCase2() {
        UUID id = UUID.randomUUID();
        UUID idDocumento = UUID.randomUUID();
        when(clienteRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(ClienteEntity.builder().id(id).build()));
        when(arquivoClienteRepository.findByIdAndClienteId(idDocumento, id)).thenReturn(Optional.empty());

        assertThrows(NaoEncontradoException.class, () -> clienteService.downloadHistorico(id, idDocumento));

        verify(documentoStorageService, never()).abrir(anyString());
    }
}
