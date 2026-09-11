package com.nowbox.nowbox_api.modules.permissao.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoCreateDTO;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoFilterDTO;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoResponseDTO;
import com.nowbox.nowbox_api.modules.permissao.service.PermissaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/permissao")
@RequiredArgsConstructor
public class PermissaoController {

    private final PermissaoService permissaoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    private Page<PermissaoResponseDTO> listAll(Pageable pageable, @ModelAttribute PermissaoFilterDTO filtro) {
        return permissaoService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private PermissaoResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return permissaoService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private PermissaoResponseDTO create(@RequestBody PermissaoCreateDTO permissao) throws NaoEncontradoException {
        return permissaoService.create(permissao);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private PermissaoResponseDTO update(@RequestBody PermissaoCreateDTO permissao, @PathVariable UUID id) {
        return permissaoService.update(permissao, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private void delete(@PathVariable UUID id) {
        permissaoService.delete(id);
    }

}
