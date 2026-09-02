package com.nowbox.nowbox_api.modules.estado.controller;

import com.nowbox.nowbox_api.common.exception.EstadoNaoEncontradoException;
import com.nowbox.nowbox_api.modules.estado.dto.EstadoResponseDTO;
import com.nowbox.nowbox_api.modules.estado.service.EstadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/estado")
@RequiredArgsConstructor
public class EstadoController {

    private final EstadoService estadoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PagedModel<EstadoResponseDTO> findAll(Pageable pageable) {
        return new PagedModel<>(estadoService.listAll(pageable));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EstadoResponseDTO find(@PathVariable UUID id) {
        return estadoService.find(id).orElseThrow(() -> new EstadoNaoEncontradoException());
    }

}
