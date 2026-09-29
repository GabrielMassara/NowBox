package com.nowbox.nowbox_api.modules.contrato.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoDownloadDTO;
import com.nowbox.nowbox_api.modules.contrato.dto.ContratoResponseDTO;
import com.nowbox.nowbox_api.modules.contrato.service.ContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/contrato")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    // Historico dos contratos de um aluguel especifico
    @GetMapping("/aluguel/{idAluguel}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_ALUGUEL_OPE_CONSULTAR_CONTRATO') and @acessoUnidadeService.temAcessoAluguel(#idAluguel)")
    public Page<ContratoResponseDTO> listByAluguel(Pageable pageable, @PathVariable UUID idAluguel) throws NaoEncontradoException {
        return contratoService.listByAluguel(pageable, idAluguel);
    }

    // Historico dos contratos de todos os alugueis de um box
    @GetMapping("/box/{idBox}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_ALUGUEL_OPE_CONSULTAR_CONTRATO') and @acessoUnidadeService.temAcessoBox(#idBox)")
    public Page<ContratoResponseDTO> listByBox(Pageable pageable, @PathVariable UUID idBox) throws NaoEncontradoException {
        return contratoService.listByBox(pageable, idBox);
    }

    // Contrato atual do aluguel. Ele pega o ultimo gerado
    @GetMapping("/aluguel/{idAluguel}/atual/download")
    @PreAuthorize("hasAuthority('MOD_ALUGUEL_OPE_BAIXAR_CONTRATO') and @acessoUnidadeService.temAcessoAluguel(#idAluguel)")
    public ResponseEntity<InputStreamResource> downloadAtual(@PathVariable UUID idAluguel) throws NaoEncontradoException {
        return toResponse(contratoService.downloadAtual(idAluguel));
    }

    // Uma versao especifica do historico
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('MOD_ALUGUEL_OPE_BAIXAR_CONTRATO') and @acessoUnidadeService.temAcessoContrato(#id)")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID id) throws NaoEncontradoException {
        return toResponse(contratoService.download(id));
    }

    private ResponseEntity<InputStreamResource> toResponse(ContratoDownloadDTO contrato) {
        ResponseEntity.BodyBuilder resposta = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(contrato.nomeArquivo()).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .contentType(MediaType.parseMediaType(contrato.contentType()));

        if (contrato.tamanho() != null) {
            resposta.contentLength(contrato.tamanho());
        }

        return resposta.body(new InputStreamResource(contrato.conteudo()));
    }
}
