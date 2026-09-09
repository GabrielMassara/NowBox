package com.nowbox.nowbox_api.modules.operacao.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.operacao.dto.OperacaoCreateDTO;
import com.nowbox.nowbox_api.modules.operacao.dto.OperacaoFilterDTO;
import com.nowbox.nowbox_api.modules.operacao.dto.OperacaoResponseDTO;
import com.nowbox.nowbox_api.modules.operacao.service.OperacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/operacao")
@RequiredArgsConstructor
public class OperacaoController {

    private final OperacaoService operacaoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    private Page<OperacaoResponseDTO> listAll(Pageable pageable, @ModelAttribute OperacaoFilterDTO filtro) {
        return operacaoService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private OperacaoResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return operacaoService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private OperacaoResponseDTO create(@RequestBody OperacaoCreateDTO operacao) throws NaoEncontradoException {
        return operacaoService.create(operacao);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private OperacaoResponseDTO update(@RequestBody OperacaoCreateDTO operacao, @PathVariable UUID id) {
        return operacaoService.update(operacao, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private void delete(@PathVariable UUID id) {
        operacaoService.delete(id);
    }

}
