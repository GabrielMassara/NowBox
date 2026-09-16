package com.nowbox.nowbox_api.modules.sessao.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.sessao.dto.SessaoCreateDTO;
import com.nowbox.nowbox_api.modules.sessao.dto.SessaoRequestFilterDTO;
import com.nowbox.nowbox_api.modules.sessao.dto.SessaoResponseDTO;
import com.nowbox.nowbox_api.modules.sessao.service.SessaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/sessao")
@RequiredArgsConstructor
public class SessaoController {

    private final SessaoService sessaoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_SESSAO_OPE_CONSULTAR')")
    public PagedModel<SessaoResponseDTO> listAll(Pageable pageable, @ModelAttribute SessaoRequestFilterDTO filtro) {
        return new PagedModel<>(sessaoService.listAll(pageable, filtro));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_SESSAO_OPE_CONSULTAR')")
    public SessaoResponseDTO listById(@PathVariable UUID id) {
        return sessaoService.listById(id).orElseThrow(() -> new NaoEncontradoException("Sessão não encontrada."));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MOD_SESSAO_OPE_CADASTRAR')")
    public SessaoResponseDTO create(@Valid @RequestBody SessaoCreateDTO content) {
        return sessaoService.save(content);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MOD_SESSAO_OPE_ATUALIZAR')")
    public void update(@Valid @RequestBody SessaoCreateDTO content, @PathVariable UUID id) {
        sessaoService.update(id, content);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MOD_SESSAO_OPE_EXCLUIR')")
    public void delete(@PathVariable UUID id) {
        sessaoService.delete(id);
    }

}
