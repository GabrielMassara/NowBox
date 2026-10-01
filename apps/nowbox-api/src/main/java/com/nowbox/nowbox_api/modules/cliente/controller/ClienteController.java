package com.nowbox.nowbox_api.modules.cliente.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteCreateDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteFilterDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteResponseDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.DocumentoDownloadDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.DocumentoHistoricoDTO;
import com.nowbox.nowbox_api.modules.cliente.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/v1/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_CONSULTAR')")
    public Page<ClienteResponseDTO> listAll(Pageable pageable, @ModelAttribute ClienteFilterDTO filtro) {
        return clienteService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_CONSULTAR')")
    public ClienteResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return clienteService.find(pageable, id);
    }

    @GetMapping("/{id}/documento")
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_CONSULTAR')")
    public ResponseEntity<InputStreamResource> downloadDocumento(@PathVariable UUID id) throws NaoEncontradoException {
        return toResponse(clienteService.downloadDocumento(id));
    }

    @GetMapping("/{id}/documentos")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_CONSULTAR')")
    public Page<DocumentoHistoricoDTO> listDocumentos(Pageable pageable, @PathVariable UUID id) throws NaoEncontradoException {
        return clienteService.listDocumentos(pageable, id);
    }

    @GetMapping("/{id}/documentos/{idDocumento}")
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_CONSULTAR')")
    public ResponseEntity<InputStreamResource> downloadHistorico(@PathVariable UUID id, @PathVariable UUID idDocumento) throws NaoEncontradoException {
        return toResponse(clienteService.downloadHistorico(id, idDocumento));
    }

    private ResponseEntity<InputStreamResource> toResponse(DocumentoDownloadDTO documento) {
        ResponseEntity.BodyBuilder resposta = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(documento.nomeArquivo()).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .contentType(MediaType.parseMediaType(documento.contentType()));

        if (documento.tamanho() != null) {
            resposta.contentLength(documento.tamanho());
        }

        return resposta.body(new InputStreamResource(documento.conteudo()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_CADASTRAR')")
    public ClienteResponseDTO create(@RequestPart("cliente") ClienteCreateDTO cliente,
                                     @RequestPart(value = "documento", required = false) MultipartFile documento) throws NaoEncontradoException {
        return clienteService.create(cliente, documento);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_ATUALIZAR')")
    public ClienteResponseDTO update(@RequestPart("cliente") ClienteCreateDTO cliente,
                                     @RequestPart(value = "documento", required = false) MultipartFile documento,
                                     @PathVariable UUID id) {
        return clienteService.update(cliente, documento, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_EXCLUIR')")
    public void delete(@PathVariable UUID id) {
        clienteService.delete(id);
    }

}
