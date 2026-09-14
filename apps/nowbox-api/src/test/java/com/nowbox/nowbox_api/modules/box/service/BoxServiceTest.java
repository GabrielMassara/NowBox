package com.nowbox.nowbox_api.modules.box.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.box.dto.BoxCreateDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxFilterDTO;
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
        when(boxRepository.findAllByFilter(idUnidade, "101", true, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<BoxResponseDTO> result = boxService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNumero()).isEqualTo("101");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(boxRepository).findAllByFilter(idUnidade, "101", true, null);
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
        when(boxRepository.findAllByFilter(null, "101", null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<BoxResponseDTO> result = boxService.listAllByFilter(null, filtro);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNumero()).isEqualTo("101");

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(boxRepository).findAllByFilter(null, "101", null, null);
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
        when(boxRepository.findAllByFilter(null, null, null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter passando filtro nulo
        Page<BoxResponseDTO> result = boxService.listAllByFilter(null, null);

        assertThat(result.getContent()).hasSize(2);

        // verifica se ao chamar a findAllByFilter ele passou todos os parametros nulos
        verify(boxRepository).findAllByFilter(null, null, null, null);
    }

    @Test
    @DisplayName("Should not list box when filter does not match")
    void listAllByFilterCase4() {
        // inicializa o filtro
        BoxFilterDTO filtro = BoxFilterDTO.builder().numero("Box Inexistente").build();

        // Mock para simular resposta do Repository
        Page<BoxEntity> paginaMock = Page.empty();

        // Quando chamar findAllByFilter ele retorna o mock paginaMock
        when(boxRepository.findAllByFilter(null, "Box Inexistente", null, null)).thenReturn(paginaMock);

        // chama a funcao listAllByFilter
        Page<BoxResponseDTO> result = boxService.listAllByFilter(null, filtro);

        // verifica se esta vazio
        assertThat(result.getContent()).isEmpty();

        // verifica se ao chamar a findAllByFilter ele passou os mesmos parametros
        verify(boxRepository).findAllByFilter(null, "Box Inexistente", null, null);
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
