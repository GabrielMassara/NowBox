package com.nowbox.nowbox_api.modules.modulo.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.modulo.dto.ModuloCreateDTO;
import com.nowbox.nowbox_api.modules.modulo.dto.ModuloFilterDTO;
import com.nowbox.nowbox_api.modules.modulo.dto.ModuloResponseDTO;
import com.nowbox.nowbox_api.modules.modulo.service.ModuloService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/modulo")
@RequiredArgsConstructor
public class ModuloController {

    private final ModuloService moduloService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_MODULO_OPE_CONSULTAR')")
    public Page<ModuloResponseDTO> listAll(Pageable pageable, @ModelAttribute ModuloFilterDTO filtro) {
        return moduloService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_MODULO_OPE_CONSULTAR')")
    public ModuloResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return moduloService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MOD_MODULO_OPE_CADASTRAR')")
    public ModuloResponseDTO create(@RequestBody ModuloCreateDTO modulo) throws NaoEncontradoException {
        return moduloService.create(modulo);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_MODULO_OPE_ATUALIZAR')")
    public ModuloResponseDTO update(@RequestBody ModuloCreateDTO modulo, @PathVariable UUID id) {
        return moduloService.update(modulo, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MOD_MODULO_OPE_EXCLUIR')")
    public void delete(@PathVariable UUID id) {
        moduloService.delete(id);
    }

}
