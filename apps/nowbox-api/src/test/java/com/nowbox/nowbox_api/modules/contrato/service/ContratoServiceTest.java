package com.nowbox.nowbox_api.modules.contrato.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoDownloadDTO;
import com.nowbox.nowbox_api.modules.contrato.dto.AditivoResponseDTO;
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
    @DisplayName("Should list the aditivos history of an aluguel")
    void listAditivosByAluguelCase1() {
        UUID idAluguel = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 10);
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.of(AluguelEntity.builder().id(idAluguel).build()));
        when(arquivoAluguelRepository.findByAluguelIdOrderBySalvoEmDesc(idAluguel, pageable))
                .thenReturn(new PageImpl<>(List.of(contrato(UUID.randomUUID(), idAluguel, UUID.randomUUID()))));

        Page<AditivoResponseDTO> result = contratoService.listAditivosByAluguel(pageable, idAluguel);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getIdAluguel()).isEqualTo(idAluguel);
        assertThat(result.getContent().getFirst().getNumeroBox()).isEqualTo("101");
        assertThat(result.getContent().getFirst().getNomeCliente()).isEqualTo("Cliente");
        assertThat(result.getContent().getFirst().getNomeArquivo()).isEqualTo("contrato-teste.pdf");
    }

    @Test
    @DisplayName("Should throw NaoEncontradoException when listing contratos of an aluguel that does not exist")
    void listAditivosByAluguelCase2() {
        UUID idAluguel = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.empty());

        assertThrows(NaoEncontradoException.class, () -> contratoService.listAditivosByAluguel(PageRequest.of(0, 10), idAluguel));

        verify(arquivoAluguelRepository, never()).findByAluguelIdOrderBySalvoEmDesc(any(), any());
    }

    @Test
    @DisplayName("Should open the file of a specific aditivo from the storage")
    void downloadCase1() {
        UUID id = UUID.randomUUID();
        InputStream conteudo = new ByteArrayInputStream(new byte[]{1, 2, 3});
        when(arquivoAluguelRepository.findById(id)).thenReturn(Optional.of(contrato(id, UUID.randomUUID(), UUID.randomUUID())));
        when(storageService.abrir("contratos/a.pdf")).thenReturn(conteudo);

        ContratoDownloadDTO result = contratoService.downloadAditivo(id);

        assertThat(result.nomeArquivo()).isEqualTo("contrato-teste.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.tamanho()).isEqualTo(1234L);
        assertThat(result.conteudo()).isSameAs(conteudo);
    }

    @Test
    @DisplayName("Should throw NaoEncontradoException when aditivo does not exist")
    void downloadCase2() {
        UUID id = UUID.randomUUID();
        when(arquivoAluguelRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NaoEncontradoException.class, () -> contratoService.downloadAditivo(id));

        verify(storageService, never()).abrir(any());
    }

    @Test
    @DisplayName("Should open the original contrato referenced by the aluguel")
    void downloadContratoCase1() {
        UUID idAluguel = UUID.randomUUID();
        ArquivoEntity original = ArquivoEntity.builder().id(UUID.randomUUID()).bucket("nowbox-contratos").chave("contratos/original.pdf")
                .nomeOriginal("contrato-original.pdf").contentType("application/pdf").tamanho(10L).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.of(AluguelEntity.builder().id(idAluguel).contrato(original).build()));
        when(storageService.abrir("contratos/original.pdf")).thenReturn(new ByteArrayInputStream(new byte[0]));

        ContratoDownloadDTO result = contratoService.downloadContrato(idAluguel);

        assertThat(result.nomeArquivo()).isEqualTo("contrato-original.pdf");
        verify(arquivoAluguelRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw NaoEncontradoException when the contrato of the aluguel was not generated yet")
    void downloadContratoCase2() {
        UUID idAluguel = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.of(AluguelEntity.builder().id(idAluguel).build()));

        assertThrows(NaoEncontradoException.class, () -> contratoService.downloadContrato(idAluguel));

        verify(storageService, never()).abrir(any());
    }

    @Test
    @DisplayName("Should throw NaoEncontradoException when downloading the contrato of an aluguel that does not exist")
    void downloadContratoCase3() {
        UUID idAluguel = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.empty());

        assertThrows(NaoEncontradoException.class, () -> contratoService.downloadContrato(idAluguel));
    }

}
