package com.nowbox.nowbox_api.modules.usuario.controller;

import com.nowbox.nowbox_api.modules.usuario.dto.UsuarioCreateDTO;
import com.nowbox.nowbox_api.modules.usuario.dto.UsuarioFilterDTO;
import com.nowbox.nowbox_api.modules.usuario.dto.UsuarioResponseDTO;
import com.nowbox.nowbox_api.modules.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    private Page<UsuarioResponseDTO> listAll(Pageable pageable, @ModelAttribute UsuarioFilterDTO filtro) {
        return usuarioService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private UsuarioResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return usuarioService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private UsuarioResponseDTO create(@RequestBody UsuarioCreateDTO usuario) {
        return usuarioService.create(usuario);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private UsuarioResponseDTO update(@RequestBody UsuarioCreateDTO usuario, @PathVariable UUID id) {
        return usuarioService.update(usuario, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private void delete(@PathVariable UUID id) {
        usuarioService.delete(id);
    }

}
