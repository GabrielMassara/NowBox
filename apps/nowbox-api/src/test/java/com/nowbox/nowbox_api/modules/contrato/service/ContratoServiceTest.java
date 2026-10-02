package com.nowbox.nowbox_api.modules.contrato.service;

import com.nowbox.nowbox_api.common.exception.ConflitoException;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.common.exception.RequisicaoInvalidaException;
import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoDownloadDTO;
import com.nowbox.nowbox_api.modules.contrato.dto.AditivoResponseDTO;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoAluguelEntity;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import com.nowbox.nowbox_api.modules.contrato.repository.IArquivoAluguelRepository;
import com.nowbox.nowbox_api.modules.contrato.repository.IArquivoRepository;
import com.nowbox.nowbox_api.modules.email.messaging.EmailSolicitadoMessage;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import com.nowbox.nowbox_api.modules.contrato.storage.ContratoStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
    private IArquivoRepository arquivoRepository;

    @Mock
    private ContratoStorageService storageService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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

    @Test
    @DisplayName("Should open the distrato referenced by the aluguel")
    void downloadDistratoCase1() {
        UUID idAluguel = UUID.randomUUID();
        ArquivoEntity distrato = ArquivoEntity.builder().id(UUID.randomUUID()).bucket("nowbox-contratos").chave("contratos/distrato.pdf")
                .nomeOriginal("distrato.pdf").contentType("application/pdf").tamanho(10L).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.of(AluguelEntity.builder().id(idAluguel).distrato(distrato).build()));
        when(storageService.abrir("contratos/distrato.pdf")).thenReturn(new ByteArrayInputStream(new byte[0]));

        ContratoDownloadDTO result = contratoService.downloadDistrato(idAluguel);

        assertThat(result.nomeArquivo()).isEqualTo("distrato.pdf");
    }

    @Test
    @DisplayName("Should throw NaoEncontradoException when the distrato of the aluguel was not generated yet")
    void downloadDistratoCase2() {
        UUID idAluguel = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.of(AluguelEntity.builder().id(idAluguel).build()));

        assertThrows(NaoEncontradoException.class, () -> contratoService.downloadDistrato(idAluguel));

        verify(storageService, never()).abrir(any());
    }

    private static final byte[] PDF = "%PDF-1.7 conteudo".getBytes();

    private AluguelEntity aluguelParaAssinar(UUID id, StatusAluguel status, String email) {
        BoxEntity box = BoxEntity.builder().id(UUID.randomUUID()).numero("101")
                .unidade(UnidadeEntity.builder().id(UUID.randomUUID()).nome("Unidade").cnpj("11111111111111").build()).build();
        ClienteEntity cliente = ClienteEntity.builder().nome("Maria").email(email).build();
        ArquivoEntity gerado = ArquivoEntity.builder().id(UUID.randomUUID()).build();

        return AluguelEntity.builder().id(id).box(box).cliente(cliente).valor(java.math.BigDecimal.TEN).status(status)
                .contrato(gerado).distrato(gerado).build();
    }

    @Test
    @DisplayName("Should store the signed contract, activate the aluguel and request the registered email")
    void enviarContratoAssinadoCase1() {
        UUID id = UUID.randomUUID();
        AluguelEntity aluguel = aluguelParaAssinar(id, StatusAluguel.PENDENTE_ASSINATURA_CONTRATO, "maria@email.com");
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguel));
        when(storageService.getBucketContratos()).thenReturn("nowbox-contratos");
        when(arquivoRepository.save(any(ArquivoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        contratoService.enviarContratoAssinado(id, new MockMultipartFile("arquivo", "assinado.pdf", "application/pdf", PDF));

        verify(storageService).gravar(argThat(chave -> chave.contains("/contratos-assinados/")), any(InputStream.class), anyLong(), anyString());
        assertThat(aluguel.getStatus()).isEqualTo(StatusAluguel.ATIVO);
        assertThat(aluguel.getContratoAssinado().getNomeOriginal()).isEqualTo("assinado.pdf");
        verify(aluguelRepository).save(aluguel);
        verify(eventPublisher).publishEvent(argThat((Object e) -> e instanceof EmailSolicitadoMessage m
                && m.template() == EmailSolicitadoMessage.Template.ALUGUEL_REGISTRADO && m.variaveis().get("situacao").equals("Ativo")));
    }

    @Test
    @DisplayName("Should reject a signed contract that is not a PDF")
    void enviarContratoAssinadoCase2() {
        UUID id = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguelParaAssinar(id, StatusAluguel.PENDENTE_ASSINATURA_CONTRATO, null)));

        assertThrows(RequisicaoInvalidaException.class, () ->
                contratoService.enviarContratoAssinado(id, new MockMultipartFile("arquivo", "x.pdf", "application/pdf", "nao e pdf".getBytes())));

        verify(storageService, never()).gravar(anyString(), any(InputStream.class), anyLong(), anyString());
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject a signed contract when the aluguel is not pending the contract signature")
    void enviarContratoAssinadoCase3() {
        UUID id = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguelParaAssinar(id, StatusAluguel.ATIVO, null)));

        assertThrows(ConflitoException.class, () ->
                contratoService.enviarContratoAssinado(id, new MockMultipartFile("arquivo", "x.pdf", "application/pdf", PDF)));

        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject a signed contract while the contract was not generated yet")
    void enviarContratoAssinadoCase4() {
        UUID id = UUID.randomUUID();
        AluguelEntity aluguel = aluguelParaAssinar(id, StatusAluguel.PENDENTE_ASSINATURA_CONTRATO, null);
        aluguel.setContrato(null);
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguel));

        assertThrows(ConflitoException.class, () ->
                contratoService.enviarContratoAssinado(id, new MockMultipartFile("arquivo", "x.pdf", "application/pdf", PDF)));

        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when sending the signed contract of an aluguel that does not exist")
    void enviarContratoAssinadoCase5() {
        UUID id = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        assertThrows(NaoEncontradoException.class, () ->
                contratoService.enviarContratoAssinado(id, new MockMultipartFile("arquivo", "x.pdf", "application/pdf", PDF)));
    }

    @Test
    @DisplayName("Should store the signed distrato, deactivate the aluguel and request the ended email")
    void enviarDistratoAssinadoCase1() {
        UUID id = UUID.randomUUID();
        AluguelEntity aluguel = aluguelParaAssinar(id, StatusAluguel.PENDENTE_ASSINATURA_DISTRATO, "maria@email.com");
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguel));
        when(storageService.getBucketContratos()).thenReturn("nowbox-contratos");
        when(arquivoRepository.save(any(ArquivoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        contratoService.enviarDistratoAssinado(id, new MockMultipartFile("arquivo", "distrato.pdf", "application/pdf", PDF));

        verify(storageService).gravar(argThat(chave -> chave.contains("/distratos-assinados/")), any(InputStream.class), anyLong(), anyString());
        assertThat(aluguel.getStatus()).isEqualTo(StatusAluguel.INATIVO);
        assertThat(aluguel.getDistratoAssinado()).isNotNull();
        verify(eventPublisher).publishEvent(argThat((Object e) -> e instanceof EmailSolicitadoMessage m
                && m.template() == EmailSolicitadoMessage.Template.ALUGUEL_ENCERRADO && m.variaveis().get("situacao").equals("Inativo")));
    }

    @Test
    @DisplayName("Should reject a signed distrato when the aluguel is not pending the distrato signature")
    void enviarDistratoAssinadoCase2() {
        UUID id = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguelParaAssinar(id, StatusAluguel.ATIVO, null)));

        assertThrows(ConflitoException.class, () ->
                contratoService.enviarDistratoAssinado(id, new MockMultipartFile("arquivo", "x.pdf", "application/pdf", PDF)));

        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should store the signed aditivo and make the aluguel active again")
    void enviarAditivoAssinadoCase1() {
        UUID id = UUID.randomUUID();
        AluguelEntity aluguel = aluguelParaAssinar(id, StatusAluguel.PENDENTE_ASSINATURA_ADITIVO, null);
        ArquivoAluguelEntity aditivo = contrato(UUID.randomUUID(), id, UUID.randomUUID());
        aditivo.setPendenteAssinatura(true);
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguel));
        when(arquivoAluguelRepository.findFirstByAluguelIdAndPendenteAssinaturaTrueOrderBySalvoEmDesc(id)).thenReturn(Optional.of(aditivo));
        when(storageService.getBucketContratos()).thenReturn("nowbox-contratos");
        when(arquivoRepository.save(any(ArquivoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        contratoService.enviarAditivoAssinado(id, new MockMultipartFile("arquivo", "aditivo.pdf", "application/pdf", PDF));

        verify(storageService).gravar(argThat(chave -> chave.contains("/aditivos-assinados/")), any(InputStream.class), anyLong(), anyString());
        assertThat(aditivo.getAssinado()).isNotNull();
        assertThat(aditivo.isPendenteAssinatura()).isFalse();
        assertThat(aluguel.getStatus()).isEqualTo(StatusAluguel.ATIVO);
        verify(arquivoAluguelRepository).save(aditivo);
        verify(aluguelRepository).save(aluguel);
    }

    @Test
    @DisplayName("Should reject a signed aditivo when the aluguel is not pending the aditivo signature")
    void enviarAditivoAssinadoCase2() {
        UUID id = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguelParaAssinar(id, StatusAluguel.ATIVO, null)));

        assertThrows(ConflitoException.class, () ->
                contratoService.enviarAditivoAssinado(id, new MockMultipartFile("arquivo", "x.pdf", "application/pdf", PDF)));

        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject a signed aditivo while the aditivo was not generated yet")
    void enviarAditivoAssinadoCase3() {
        UUID id = UUID.randomUUID();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(aluguelParaAssinar(id, StatusAluguel.PENDENTE_ASSINATURA_ADITIVO, null)));
        when(arquivoAluguelRepository.findFirstByAluguelIdAndPendenteAssinaturaTrueOrderBySalvoEmDesc(id)).thenReturn(Optional.empty());

        assertThrows(ConflitoException.class, () ->
                contratoService.enviarAditivoAssinado(id, new MockMultipartFile("arquivo", "x.pdf", "application/pdf", PDF)));

        verify(aluguelRepository, never()).save(any());
    }
}
