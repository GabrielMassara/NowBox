package com.nowbox.nowbox_api.modules.contrato.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoDownloadDTO;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoResponseDTO;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoAluguelEntity;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import com.nowbox.nowbox_api.modules.contrato.repository.IArquivoAluguelRepository;
import com.nowbox.nowbox_api.modules.contrato.storage.ContratoStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private final IArquivoAluguelRepository arquivoAluguelRepository;
    private final IAluguelRepository aluguelRepository;
    private final IBoxRepository boxRepository;
    private final ContratoStorageService storageService;

    public Page<ContratoResponseDTO> listByAluguel(Pageable pageable, UUID idAluguel) throws NaoEncontradoException {
        aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        return arquivoAluguelRepository.findByAluguelIdOrderBySalvoEmDesc(idAluguel, pageable).map(this::toResponseDTO);
    }

    public Page<ContratoResponseDTO> listByBox(Pageable pageable, UUID idBox) throws NaoEncontradoException {
        boxRepository.findByIdAndDeletedAtIsNull(idBox)
                .orElseThrow(() -> new NaoEncontradoException("Box não encontrado"));

        return arquivoAluguelRepository.findByBoxIdOrderBySalvoEmDesc(idBox, pageable).map(this::toResponseDTO);
    }

    // Baixa uma versao especifica do historico
    public ContratoDownloadDTO download(UUID id) throws NaoEncontradoException {
        ArquivoAluguelEntity contrato = arquivoAluguelRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Contrato não encontrado"));

        return abrir(contrato);
    }

    // Baixa o contrato atual do aluguel, que e o ultimo gerado
    public ContratoDownloadDTO downloadAtual(UUID idAluguel) throws NaoEncontradoException {
        aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        ArquivoAluguelEntity contrato = arquivoAluguelRepository.findFirstByAluguelIdOrderBySalvoEmDesc(idAluguel)
                .orElseThrow(() -> new NaoEncontradoException("O contrato deste aluguel ainda não foi gerado"));

        return abrir(contrato);
    }

    private ContratoDownloadDTO abrir(ArquivoAluguelEntity contrato) {
        ArquivoEntity arquivo = contrato.getArquivo();

        return new ContratoDownloadDTO(
                arquivo.getNomeOriginal(),
                arquivo.getContentType(),
                arquivo.getTamanho(),
                storageService.abrir(arquivo.getChave())
        );
    }

    private ContratoResponseDTO toResponseDTO(ArquivoAluguelEntity entidade) {
        return ContratoResponseDTO.builder()
                .id(entidade.getId())
                .idAluguel(entidade.getAluguel().getId())
                .idBox(entidade.getBox().getId())
                .numeroBox(entidade.getBox().getNumero())
                .nomeCliente(entidade.getAluguel().getCliente().getNome())
                .nomeArquivo(entidade.getArquivo().getNomeOriginal())
                .tamanho(entidade.getArquivo().getTamanho())
                .salvoEm(entidade.getSalvoEm())
                .build();
    }
}
