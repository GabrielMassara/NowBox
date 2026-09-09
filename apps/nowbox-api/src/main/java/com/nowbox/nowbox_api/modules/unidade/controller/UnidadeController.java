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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/unidade")
@RequiredArgsConstructor
public class UnidadeController {

    private final UnidadeService unidadeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    private Page<UnidadeResponseDTO> listAll(Pageable pageable, @ModelAttribute UnidadeFilterDTO filtro) {
        return unidadeService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private UnidadeResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return unidadeService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private UnidadeResponseDTO create(@RequestBody UnidadeCreateDTO unidade) throws NaoEncontradoException {
        return unidadeService.create(unidade);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private UnidadeResponseDTO update(@RequestBody UnidadeCreateDTO unidade, @PathVariable UUID id) {
        return unidadeService.update(unidade, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private void delete(@PathVariable UUID id) {
        unidadeService.delete(id);
    }

}
