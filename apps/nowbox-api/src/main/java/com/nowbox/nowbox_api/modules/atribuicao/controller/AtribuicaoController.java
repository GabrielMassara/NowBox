package com.nowbox.nowbox_api.modules.atribuicao.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.atribuicao.dto.AtribuicaoCreateDTO;
import com.nowbox.nowbox_api.modules.atribuicao.dto.AtribuicaoFilterDTO;
import com.nowbox.nowbox_api.modules.atribuicao.dto.AtribuicaoResponseDTO;
import com.nowbox.nowbox_api.modules.atribuicao.service.AtribuicaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/atribuicao")
@RequiredArgsConstructor
public class AtribuicaoController {

    private final AtribuicaoService atribuicaoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    private Page<AtribuicaoResponseDTO> listAll(Pageable pageable, @ModelAttribute AtribuicaoFilterDTO filtro) {
        return atribuicaoService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private AtribuicaoResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return atribuicaoService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private AtribuicaoResponseDTO create(@RequestBody AtribuicaoCreateDTO atribuicao) throws NaoEncontradoException {
        return atribuicaoService.create(atribuicao);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private AtribuicaoResponseDTO update(@RequestBody AtribuicaoCreateDTO atribuicao, @PathVariable UUID id) {
        return atribuicaoService.update(atribuicao, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private void delete(@PathVariable UUID id) {
        atribuicaoService.delete(id);
    }

}
