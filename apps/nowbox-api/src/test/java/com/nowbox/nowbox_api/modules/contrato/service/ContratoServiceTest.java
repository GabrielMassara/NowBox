package com.nowbox.nowbox_api.modules.contrato.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoDownloadDTO;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoResponseDTO;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoAluguelEntity;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import com.nowbox.nowbox_api.modules.contrato.repository.IArquivoAluguelRepository;
import com.nowbox.nowbox_api.modules.contrato.storage.ContratoStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
class ContratoServiceTest {

    @Mock
    private IArquivoAluguelRepository arquivoAluguelRepository;

    @Mock
    private IAluguelRepository aluguelRepository;

    @Mock
    private IBoxRepository boxRepository;

    @Mock
    private ContratoStorageService storageService;

    @InjectMocks
    private ContratoService contratoService;

    private ArquivoAluguelEntity contrato(UUID id, UUID idAluguel, UUID idBox) {
        BoxEntity box = BoxEntity.builder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().nome("Cliente").build();
        AluguelEntity aluguel = AluguelEntity.builder().id(idAluguel).box(box).cliente(cliente).build();
        ArquivoEntity arquivo = ArquivoEntity.builder().id(UUID.randomUUID()).bucket("nowbox-contratos").chave("contratos/a.pdf")
                .nomeOriginal("contrato-teste.pdf").contentType("application/pdf").tamanho(1234L).build();

        return ArquivoAluguelEntity.builder().id(id).arquivo(arquivo).aluguel(aluguel).box(box).salvoEm(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("Should list the contratos history of an aluguel")
    void listByAluguelCase1() {
        UUID idAluguel = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 10);
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.of(AluguelEntity.builder().id(idAluguel).build()));
        when(arquivoAluguelRepository.findByAluguelIdOrderBySalvoEmDesc(idAluguel, pageable))
                .thenReturn(new PageImpl<>(List.of(contrato(UUID.randomUUID(), idAluguel, UUID.randomUUID()))));

        Page<ContratoResponseDTO> result = contratoService.listByAluguel(pageable, idAluguel);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getIdAluguel()).isEqualTo(idAluguel);
        assertThat(result.getContent().getFirst().getNumeroBox()).isEqualTo("101");
        assertThat(result.getContent().getFirst().getNomeCliente()).isEqualTo("Cliente");
        assertThat(result.getContent().getFirst().getNomeArquivo()).isEqualTo("contrato-teste.pdf");
    }

    @Test
    @DisplayName("Should throw NaoEncontradoException when listing contratos of an aluguel that does not exist")
    void listByAluguelCase2() {
        UUID idAluguel = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.empty());

        assertThrows(NaoEncontradoException.class, () -> contratoService.listByAluguel(PageRequest.of(0, 10), idAluguel));

        verify(arquivoAluguelRepository, never()).findByAluguelIdOrderBySalvoEmDesc(any(), any());
    }

    @Test
    @DisplayName("Should list the contratos history of a box")
    void listByBoxCase1() {
        UUID idBox = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 10);
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(BoxEntity.builder().id(idBox).build()));
        when(arquivoAluguelRepository.findByBoxIdOrderBySalvoEmDesc(idBox, pageable))
                .thenReturn(new PageImpl<>(List.of(contrato(UUID.randomUUID(), UUID.randomUUID(), idBox))));

        Page<ContratoResponseDTO> result = contratoService.listByBox(pageable, idBox);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getIdBox()).isEqualTo(idBox);
    }

    @Test
    @DisplayName("Should throw NaoEncontradoException when listing contratos of a box that does not exist")
    void listByBoxCase2() {
        UUID idBox = UUID.randomUUID();
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.empty());

        assertThrows(NaoEncontradoException.class, () -> contratoService.listByBox(PageRequest.of(0, 10), idBox));
    }

    @Test
    @DisplayName("Should open the file of a specific contrato from the storage")
    void downloadCase1() {
        UUID id = UUID.randomUUID();
        InputStream conteudo = new ByteArrayInputStream(new byte[]{1, 2, 3});
        when(arquivoAluguelRepository.findById(id)).thenReturn(Optional.of(contrato(id, UUID.randomUUID(), UUID.randomUUID())));
        when(storageService.abrir("contratos/a.pdf")).thenReturn(conteudo);

        ContratoDownloadDTO result = contratoService.download(id);

        assertThat(result.nomeArquivo()).isEqualTo("contrato-teste.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.tamanho()).isEqualTo(1234L);
        assertThat(result.conteudo()).isSameAs(conteudo);
    }

    @Test
    @DisplayName("Should throw NaoEncontradoException when contrato does not exist")
    void downloadCase2() {
        UUID id = UUID.randomUUID();
        when(arquivoAluguelRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NaoEncontradoException.class, () -> contratoService.download(id));

        verify(storageService, never()).abrir(any());
    }

    @Test
    @DisplayName("Should open the latest contrato of the aluguel as the current one")
    void downloadAtualCase1() {
        UUID idAluguel = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.of(AluguelEntity.builder().id(idAluguel).build()));
        when(arquivoAluguelRepository.findFirstByAluguelIdOrderBySalvoEmDesc(idAluguel))
                .thenReturn(Optional.of(contrato(UUID.randomUUID(), idAluguel, UUID.randomUUID())));
        when(storageService.abrir("contratos/a.pdf")).thenReturn(new ByteArrayInputStream(new byte[0]));

        ContratoDownloadDTO result = contratoService.downloadAtual(idAluguel);

        assertThat(result.nomeArquivo()).isEqualTo("contrato-teste.pdf");
    }

    @Test
    @DisplayName("Should throw NaoEncontradoException when the contrato of the aluguel was not generated yet")
    void downloadAtualCase2() {
        UUID idAluguel = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.of(AluguelEntity.builder().id(idAluguel).build()));
        when(arquivoAluguelRepository.findFirstByAluguelIdOrderBySalvoEmDesc(idAluguel)).thenReturn(Optional.empty());

        assertThrows(NaoEncontradoException.class, () -> contratoService.downloadAtual(idAluguel));
    }

}
