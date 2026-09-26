package com.nowbox.nowbox_api.modules.box.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.common.exception.RequisicaoInvalidaException;
import com.nowbox.nowbox_api.modules.box.dto.BoxCreateDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxFilterDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxLoteDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxResponseDTO;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
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
class BoxServiceTest {

    @Mock
    private IBoxRepository boxRepository;

    @Mock
    private IUnidadeRepository unidadeRepository;

    @InjectMocks
    private BoxService boxService;

    @Test
    @DisplayName("Should list one box filtered by idUnidade, numero and disponivel")
    void listAllByFilterCase1() {
        // id da unidade utilizado no filtro
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();

        // inicializa o filtro
        BoxFilterDTO filtro = BoxFilterDTO.builder().idUnidade(idUnidade).numero("101").disponivel(true).build();

        // Mock para simular resposta do Repository
        BoxEntity entidade = BoxEntity.builder().numero("101").disponivel(true).unidade(unidade).build();
        Page<BoxEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(boxRepository.findAllByFilter(idUnidade, "101", true, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<BoxResponseDTO> result = boxService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNumero()).isEqualTo("101");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(boxRepository).findAllByFilter(idUnidade, "101", true, null, null);
    }

    @Test
    @DisplayName("Should list boxes filtered by alugado")
    void listAllByFilterCase5() {
        // id da unidade utilizado no filtro
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();

        // inicializa o filtro pedindo apenas boxes liberados que nao estao alugados
        BoxFilterDTO filtro = BoxFilterDTO.builder().idUnidade(idUnidade).disponivel(true).alugado(false).build();

        // Mock para simular resposta do Repository
        BoxEntity entidade = BoxEntity.builder().numero("101").disponivel(true).unidade(unidade).build();
        Page<BoxEntity> paginaMock = new PageImpl<>(List.of(entidade));
        when(boxRepository.findAllByFilter(idUnidade, null, true, false, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<BoxResponseDTO> result = boxService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);

        // verifica se ao chamar a findAllByFilter ele passou o filtro alugado
        verify(boxRepository).findAllByFilter(idUnidade, null, true, false, null);
    }

    @Test
    @DisplayName("Should list one box filtered by numero")
    void listAllByFilterCase2() {
        // inicializa o filtro
        BoxFilterDTO filtro = BoxFilterDTO.builder().numero("101").build();

        // Mock para simular resposta do Repository
        UnidadeEntity unidade = UnidadeEntity.builder().nome("Unidade Test").build();
        BoxEntity entidade = BoxEntity.builder().numero("101").unidade(unidade).build();
        Page<BoxEntity> paginaMock = new PageImpl<>(List.of(entidade));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(boxRepository.findAllByFilter(null, "101", null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<BoxResponseDTO> result = boxService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNumero()).isEqualTo("101");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(boxRepository).findAllByFilter(null, "101", null, null, null);
    }

    @Test
    @DisplayName("Should list all boxes when filter is null")
    void listAllByFilterCase3() {
        // Mock para simular resposta do Repository
        UnidadeEntity unidade = UnidadeEntity.builder().nome("Unidade Test").build();
        BoxEntity entidade1 = BoxEntity.builder().numero("101").unidade(unidade).build();
        BoxEntity entidade2 = BoxEntity.builder().numero("102").unidade(unidade).build();
        Page<BoxEntity> paginaMock = new PageImpl<>(List.of(entidade1, entidade2));

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(boxRepository.findAllByFilter(null, null, null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter passando filtro nulo
        Page<BoxResponseDTO> result = boxService.listAllByFilter(null, null);

        assertThat(result.getContent()).hasSize(2);

        // verifica se ao chamar a findAllByFilter ele passou todos os parametros nulos
        verify(boxRepository).findAllByFilter(null, null, null, null, null);
    }

    @Test
    @DisplayName("Should not list box when filter does not match")
    void listAllByFilterCase4() {
        // inicializa o filtro
        BoxFilterDTO filtro = BoxFilterDTO.builder().numero("Box Inexistente").build();

        // Mock para simular resposta do Repository
        Page<BoxEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(boxRepository.findAllByFilter(null, "Box Inexistente", null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<BoxResponseDTO> result = boxService.listAllByFilter(null, filtro);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(boxRepository).findAllByFilter(null, "Box Inexistente", null, null, null);
    }

    @Test
    @DisplayName("Should return box when id exists")
    void findCase1() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Repository
        UnidadeEntity unidade = UnidadeEntity.builder().nome("Unidade Test").build();
        BoxEntity entidade = BoxEntity.builder().id(id).numero("101").unidade(unidade).build();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna o mock entidade
        when(boxRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidade));

        // chama a funcao find
        BoxResponseDTO result = boxService.find(null, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNumero()).isEqualTo("101");

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(boxRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should throw exception when box id does not exist")
    void findCase2() {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar findByIdAndDeletedAtIsNull ele retorna vazio
        when(boxRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao find e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> boxService.find(null, id));

        // verifica se ao chamar a findByIdAndDeletedAtIsNull ele passou o mesmo id
        verify(boxRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    @DisplayName("Should save and return created box")
    void createCase1() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();

        // dados para criacao
        BoxCreateDTO box = BoxCreateDTO.builder()
                .idUnidade(idUnidade)
                .numero("101")
                .tamanho(BigDecimal.valueOf(10.5))
                .dimensoes("2x5")
                .disponivel(true)
                .preco(BigDecimal.valueOf(150.00))
                .build();

        // Mock para simular que a unidade existe
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));

        // Mock para simular resposta do Repository
        UUID id = UUID.randomUUID();
        BoxEntity entidadeSalva = BoxEntity.builder()
                .id(id)
                .numero("101")
                .tamanho(BigDecimal.valueOf(10.5))
                .dimensoes("2x5")
                .disponivel(true)
                .preco(BigDecimal.valueOf(150.00))
                .unidade(unidade)
                .createdAt(LocalDateTime.now())
                .build();
        when(boxRepository.save(any(BoxEntity.class))).thenReturn(entidadeSalva);

        // chama a funcao create
        BoxResponseDTO result = boxService.create(box);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNumero()).isEqualTo("101");
        assertThat(result.getPreco()).isEqualTo(BigDecimal.valueOf(150.00));
        assertThat(result.getUnidade()).isEqualTo(unidade);

        // verifica se buscou a unidade antes de criar
        verify(unidadeRepository).findByIdAndDeletedAtIsNull(idUnidade);

        // verifica se ao chamar o save ele passou uma entidade com os dados corretos
        verify(boxRepository).save(argThat(e -> e.getNumero().equals("101") && e.getPreco().equals(BigDecimal.valueOf(150.00)) && e.getUnidade().equals(unidade)));
    }

    @Test
    @DisplayName("Should throw exception when creating box with a unidade that does not exist")
    void createCase2() {
        // id da unidade inexistente
        UUID idUnidade = UUID.randomUUID();

        // dados para criacao
        BoxCreateDTO box = BoxCreateDTO.builder().idUnidade(idUnidade).numero("101").build();

        // Mock para simular que a unidade nao existe
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.empty());

        // chama a funcao create e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> boxService.create(box));

        // verifica se buscou a unidade antes de tentar criar
        verify(unidadeRepository).findByIdAndDeletedAtIsNull(idUnidade);

        // verifica se nunca chegou a salvar, ja que a unidade nao existe
        verify(boxRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create every box of the interval when none of them exists")
    void createBatchCase1() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();

        // dados para criacao em lote
        BoxLoteDTO lote = BoxLoteDTO.builder()
                .idUnidade(idUnidade).prefixo("BOX-").numeroInicial(1).numeroFinal(3)
                .tamanho(BigDecimal.valueOf(6)).dimensoes("2x3").disponivel(true).preco(BigDecimal.valueOf(200.00))
                .build();

        // Mock para simular que a unidade existe e que nenhum numero esta cadastrado
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));
        when(boxRepository.findNumerosCadastrados(idUnidade, List.of("box-1", "box-2", "box-3"))).thenReturn(List.of());
        when(boxRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // chama a funcao createBatch
        List<BoxResponseDTO> result = boxService.createBatch(lote);

        assertThat(result).extracting(BoxResponseDTO::getNumero).containsExactly("BOX-1", "BOX-2", "BOX-3");
        assertThat(result).allMatch(b -> b.getUnidade().equals(unidade) && b.getPreco().equals(BigDecimal.valueOf(200.00)));

        // verifica se ao chamar o saveAll ele passou os tres boxes com os dados em comum
        verify(boxRepository).saveAll(argThat(boxes -> {
            List<BoxEntity> lista = (List<BoxEntity>) boxes;
            return lista.size() == 3 && lista.stream().allMatch(e -> e.getDimensoes().equals("2x3") && e.getDisponivel());
        }));
    }

    @Test
    @DisplayName("Should ignore the numeros that already exist in the unidade")
    void createBatchCase2() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();

        // dados para criacao em lote
        BoxLoteDTO lote = BoxLoteDTO.builder().idUnidade(idUnidade).prefixo("BOX-").numeroInicial(1).numeroFinal(3).build();

        // Mock para simular que a unidade existe e que o box 2 ja esta cadastrado (em minusculo)
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));
        when(boxRepository.findNumerosCadastrados(idUnidade, List.of("box-1", "box-2", "box-3"))).thenReturn(List.of("box-2"));
        when(boxRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // chama a funcao createBatch
        List<BoxResponseDTO> result = boxService.createBatch(lote);

        assertThat(result).extracting(BoxResponseDTO::getNumero).containsExactly("BOX-1", "BOX-3");
    }

    @Test
    @DisplayName("Should pad the numeros with zeros when completarComZeros is true")
    void createBatchCase3() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();

        // dados para criacao em lote de 8 a 10, sem prefixo
        BoxLoteDTO lote = BoxLoteDTO.builder().idUnidade(idUnidade).numeroInicial(8).numeroFinal(10).completarComZeros(true).build();

        // Mock para simular que a unidade existe e que nenhum numero esta cadastrado
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));
        when(boxRepository.findNumerosCadastrados(any(), any())).thenReturn(List.of());
        when(boxRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // chama a funcao createBatch
        List<BoxResponseDTO> result = boxService.createBatch(lote);

        assertThat(result).extracting(BoxResponseDTO::getNumero).containsExactly("08", "09", "10");
    }

    @Test
    @DisplayName("Should return an empty list when every numero of the interval already exists")
    void createBatchCase4() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();

        // dados para criacao em lote
        BoxLoteDTO lote = BoxLoteDTO.builder().idUnidade(idUnidade).numeroInicial(1).numeroFinal(2).build();

        // Mock para simular que os dois numeros ja estao cadastrados
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));
        when(boxRepository.findNumerosCadastrados(idUnidade, List.of("1", "2"))).thenReturn(List.of("1", "2"));
        when(boxRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // chama a funcao createBatch
        List<BoxResponseDTO> result = boxService.createBatch(lote);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when creating a batch with a unidade that does not exist")
    void createBatchCase5() {
        // id da unidade inexistente
        UUID idUnidade = UUID.randomUUID();

        // dados para criacao em lote
        BoxLoteDTO lote = BoxLoteDTO.builder().idUnidade(idUnidade).numeroInicial(1).numeroFinal(3).build();

        // Mock para simular que a unidade nao existe
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.empty());

        // chama a funcao createBatch e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> boxService.createBatch(lote));

        // verifica se nunca chegou a salvar, ja que a unidade nao existe
        verify(boxRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should throw exception when the interval of the batch is invalid")
    void createBatchCase6() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));

        // numero final menor que o inicial
        BoxLoteDTO invertido = BoxLoteDTO.builder().idUnidade(idUnidade).numeroInicial(5).numeroFinal(1).build();
        assertThrows(RequisicaoInvalidaException.class, () -> boxService.createBatch(invertido));

        // intervalo ausente
        BoxLoteDTO ausente = BoxLoteDTO.builder().idUnidade(idUnidade).build();
        assertThrows(RequisicaoInvalidaException.class, () -> boxService.createBatch(ausente));

        // numero inicial negativo
        BoxLoteDTO negativo = BoxLoteDTO.builder().idUnidade(idUnidade).numeroInicial(-1).numeroFinal(3).build();
        assertThrows(RequisicaoInvalidaException.class, () -> boxService.createBatch(negativo));

        // verifica se nunca chegou a salvar
        verify(boxRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should throw exception when the batch has more boxes than the limit")
    void createBatchCase7() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));

        // 201 boxes, um acima do limite
        BoxLoteDTO lote = BoxLoteDTO.builder().idUnidade(idUnidade).numeroInicial(1).numeroFinal(201).build();

        assertThrows(RequisicaoInvalidaException.class, () -> boxService.createBatch(lote));

        // verifica se nunca chegou a salvar
        verify(boxRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should throw exception when a numero of the batch exceeds the column length")
    void createBatchCase8() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));

        // prefixo de 19 caracteres mais o numero 10 ultrapassa os 20 caracteres da coluna
        BoxLoteDTO lote = BoxLoteDTO.builder().idUnidade(idUnidade).prefixo("ABCDEFGHIJKLMNOPQRS").numeroInicial(1).numeroFinal(10).build();

        assertThrows(RequisicaoInvalidaException.class, () -> boxService.createBatch(lote));

        // verifica se nunca chegou a salvar
        verify(boxRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should create box as disponivel when the field is not informed")
    void createCase3() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();

        // dados para criacao sem informar disponivel
        BoxCreateDTO box = BoxCreateDTO.builder().idUnidade(idUnidade).numero("101").build();

        // Mock para simular que a unidade existe
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));
        when(boxRepository.save(any(BoxEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // chama a funcao create
        BoxResponseDTO result = boxService.create(box);

        assertThat(result.getDisponivel()).isTrue();
    }

    @Test
    @DisplayName("Should keep box blocked when disponivel is explicitly false")
    void createCase4() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();

        // dados para criacao com o box bloqueado
        BoxCreateDTO box = BoxCreateDTO.builder().idUnidade(idUnidade).numero("101").disponivel(false).build();

        // Mock para simular que a unidade existe
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));
        when(boxRepository.save(any(BoxEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // chama a funcao create
        BoxResponseDTO result = boxService.create(box);

        assertThat(result.getDisponivel()).isFalse();
    }

    @Test
    @DisplayName("Should create the boxes of the batch as disponivel when the field is not informed")
    void createBatchCase9() {
        // id da unidade utilizado na criacao
        UUID idUnidade = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();

        // dados para criacao em lote sem informar disponivel
        BoxLoteDTO lote = BoxLoteDTO.builder().idUnidade(idUnidade).numeroInicial(1).numeroFinal(2).build();

        // Mock para simular que a unidade existe e que nenhum numero esta cadastrado
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));
        when(boxRepository.findNumerosCadastrados(any(), any())).thenReturn(List.of());
        when(boxRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // chama a funcao createBatch
        List<BoxResponseDTO> result = boxService.createBatch(lote);

        assertThat(result).hasSize(2).allMatch(BoxResponseDTO::getDisponivel);
    }

    @Test
    @DisplayName("Should update box when id and unidade exist")
    void updateCase1() {
        // id do box e da unidade utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idUnidade = UUID.randomUUID();

        // dados para atualizacao
        BoxCreateDTO box = BoxCreateDTO.builder().idUnidade(idUnidade).numero("101").disponivel(false).build();

        // Mock para simular que o box existe
        UnidadeEntity unidadeAntiga = UnidadeEntity.builder().nome("Unidade Old").build();
        BoxEntity entidadeExistente = BoxEntity.builder().id(id).numero("100").disponivel(true).unidade(unidadeAntiga).createdAt(LocalDateTime.now()).build();
        when(boxRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que a unidade existe
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.of(unidade));

        // Mock para simular resposta do save
        BoxEntity entidadeAtualizada = BoxEntity.builder().id(id).numero("101").disponivel(false).unidade(unidade).build();
        when(boxRepository.save(any(BoxEntity.class))).thenReturn(entidadeAtualizada);

        // chama a funcao update
        BoxResponseDTO result = boxService.update(box, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNumero()).isEqualTo("101");
        assertThat(result.getDisponivel()).isFalse();
        assertThat(result.getUnidade()).isEqualTo(unidade);

        // verifica se buscou o box e a unidade antes de atualizar
        verify(boxRepository).findByIdAndDeletedAtIsNull(id);
        verify(unidadeRepository).findByIdAndDeletedAtIsNull(idUnidade);

        // verifica se salvou a entidade com os dados atualizados
        verify(boxRepository).save(argThat(e -> e.getId().equals(id) && e.getNumero().equals("101") && e.getUnidade().equals(unidade)));
    }

    @Test
    @DisplayName("Should throw exception when updating a box that does not exist")
    void updateCase2() {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        BoxCreateDTO box = BoxCreateDTO.builder().idUnidade(UUID.randomUUID()).numero("101").build();

        // Mock para simular que o box nao existe
        when(boxRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> boxService.update(box, id));

        // verifica se buscou o box antes de tentar atualizar
        verify(boxRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se nunca chegou a buscar a unidade nem a salvar
        verify(unidadeRepository, never()).findByIdAndDeletedAtIsNull(any(UUID.class));
        verify(boxRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating box with a unidade that does not exist")
    void updateCase3() {
        // id do box e da unidade utilizados na atualizacao
        UUID id = UUID.randomUUID();
        UUID idUnidade = UUID.randomUUID();
        BoxCreateDTO box = BoxCreateDTO.builder().idUnidade(idUnidade).numero("101").build();

        // Mock para simular que o box existe
        UnidadeEntity unidadeAntiga = UnidadeEntity.builder().nome("Unidade Old").build();
        BoxEntity entidadeExistente = BoxEntity.builder().id(id).numero("100").unidade(unidadeAntiga).build();
        when(boxRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // Mock para simular que a unidade nao existe
        when(unidadeRepository.findByIdAndDeletedAtIsNull(idUnidade)).thenReturn(Optional.empty());

        // chama a funcao update e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> boxService.update(box, id));

        // verifica se buscou o box e a unidade antes de tentar atualizar
        verify(boxRepository).findByIdAndDeletedAtIsNull(id);
        verify(unidadeRepository).findByIdAndDeletedAtIsNull(idUnidade);

        // verifica se nunca chegou a salvar, ja que a unidade nao existe
        verify(boxRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete box by id")
    void deleteCase1() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o box existe
        BoxEntity entidadeExistente = BoxEntity.builder().id(id).numero("101").build();
        when(boxRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(entidadeExistente));

        // chama a funcao delete
        boxService.delete(id);

        // verifica se buscou o box antes de excluir
        verify(boxRepository).findByIdAndDeletedAtIsNull(id);

        // verifica se salvou a entidade com o deletedAt preenchido
        verify(boxRepository).save(argThat(e -> e.getId().equals(id) && e.getDeletedAt() != null));
    }

    @Test
    @DisplayName("Should throw exception when deleting a box that does not exist")
    void deleteCase2() {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o box nao existe
        when(boxRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        // chama a funcao delete e verifica se lanca a excecao esperada
        assertThrows(NaoEncontradoException.class, () -> boxService.delete(id));

        // verifica se nunca chegou a salvar, ja que o box nao existe
        verify(boxRepository, never()).save(any());
    }
}
