package com.nowbox.nowbox_api.modules.contrato.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.contrato.dto.AditivoResponseDTO;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoDownloadDTO;
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
    private final ContratoStorageService storageService;

    public Page<AditivoResponseDTO> listAditivosByAluguel(Pageable pageable, UUID idAluguel) throws NaoEncontradoException {
        aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        return arquivoAluguelRepository.findByAluguelIdOrderBySalvoEmDesc(idAluguel, pageable).map(this::toResponseDTO);
    }

    // Baixa um aditivo especifico
    public ContratoDownloadDTO downloadAditivo(UUID id) throws NaoEncontradoException {
        ArquivoAluguelEntity aditivo = arquivoAluguelRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Aditivo não encontrado"));

        return abrir(aditivo.getArquivo());
    }

    // Baixa o contrato original do aluguel. Ele nao muda quando o aluguel e editado
    public ContratoDownloadDTO downloadContrato(UUID idAluguel) throws NaoEncontradoException {
        AluguelEntity aluguel = aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        if (aluguel.getContrato() == null) {
            throw new NaoEncontradoException("O contrato deste aluguel ainda não foi gerado");
        }

        return abrir(aluguel.getContrato());
    }

    private ContratoDownloadDTO abrir(ArquivoEntity arquivo) {
        return new ContratoDownloadDTO(
                arquivo.getNomeOriginal(),
                arquivo.getContentType(),
                arquivo.getTamanho(),
                storageService.abrir(arquivo.getChave())
        );
    }

    private AditivoResponseDTO toResponseDTO(ArquivoAluguelEntity entidade) {
        return AditivoResponseDTO.builder()
                .id(entidade.getId())
                .idAluguel(entidade.getAluguel().getId())
                .idBox(entidade.getBox().getId())
                .numeroBox(entidade.getBox().getNumero())
                .nomeCliente(entidade.getAluguel().getCliente().getNome())
                .nomeArquivo(entidade.getArquivo().getNomeOriginal())
                .tamanho(entidade.getArquivo().getTamanho())
                .descricao(entidade.getDescricao())
                .salvoEm(entidade.getSalvoEm())
                .build();
    }
}
