package com.nowbox.nowbox_api.modules.unidade.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.unidade.dto.UnidadeCreateDTO;
import com.nowbox.nowbox_api.modules.unidade.dto.UnidadeFilterDTO;
import com.nowbox.nowbox_api.modules.unidade.dto.UnidadeResponseDTO;
import com.nowbox.nowbox_api.modules.unidade.service.UnidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/unidade")
@RequiredArgsConstructor
public class UnidadeController {

    private final UnidadeService unidadeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_UNIDADE_OPE_CONSULTAR')")
    public Page<UnidadeResponseDTO> listAll(Pageable pageable, @ModelAttribute UnidadeFilterDTO filtro) {
        return unidadeService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_UNIDADE_OPE_CONSULTAR')")
    public UnidadeResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return unidadeService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MOD_UNIDADE_OPE_CADASTRAR')")
    public UnidadeResponseDTO create(@RequestBody UnidadeCreateDTO unidade) throws NaoEncontradoException {
        return unidadeService.create(unidade);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_UNIDADE_OPE_ATUALIZAR')")
    public UnidadeResponseDTO update(@RequestBody UnidadeCreateDTO unidade, @PathVariable UUID id) {
        return unidadeService.update(unidade, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MOD_UNIDADE_OPE_EXCLUIR')")
    public void delete(@PathVariable UUID id) {
        unidadeService.delete(id);
    }

}
