package com.nowbox.nowbox_api.modules.box.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.box.dto.BoxCreateDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxFilterDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxResponseDTO;
import com.nowbox.nowbox_api.modules.box.service.BoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/box")
@RequiredArgsConstructor
public class BoxController {

    private final BoxService boxService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_BOX_OPE_CONSULTAR') and @acessoUnidadeService.temAcesso(#filtro.idUnidade)")
    public Page<BoxResponseDTO> listAll(Pageable pageable, @ModelAttribute BoxFilterDTO filtro) {
        return boxService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_BOX_OPE_CONSULTAR') and @acessoUnidadeService.temAcessoBox(#id)")
    public BoxResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return boxService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MOD_BOX_OPE_CADASTRAR') and @acessoUnidadeService.temAcesso(#box.idUnidade)")
    public BoxResponseDTO create(@RequestBody BoxCreateDTO box) throws NaoEncontradoException {
        return boxService.create(box);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_BOX_OPE_ATUALIZAR') and @acessoUnidadeService.temAcessoBox(#id) and @acessoUnidadeService.temAcesso(#box.idUnidade)")
    public BoxResponseDTO update(@RequestBody BoxCreateDTO box, @PathVariable UUID id) {
        return boxService.update(box, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MOD_BOX_OPE_EXCLUIR') and @acessoUnidadeService.temAcessoBox(#id)")
    public void delete(@PathVariable UUID id) {
        boxService.delete(id);
    }

}
