package com.nowbox.nowbox_api.modules.aluguel.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelCreateDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelFilterDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelResponseDTO;
import com.nowbox.nowbox_api.modules.aluguel.service.AluguelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/aluguel")
@RequiredArgsConstructor
public class AluguelController {

    private final AluguelService aluguelService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    private Page<AluguelResponseDTO> listAll(Pageable pageable, @ModelAttribute AluguelFilterDTO filtro) {
        return aluguelService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private AluguelResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return aluguelService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private AluguelResponseDTO create(@RequestBody AluguelCreateDTO aluguel) throws NaoEncontradoException {
        return aluguelService.create(aluguel);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private AluguelResponseDTO update(@RequestBody AluguelCreateDTO aluguel, @PathVariable UUID id) {
        return aluguelService.update(aluguel, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private void delete(@PathVariable UUID id) {
        aluguelService.delete(id);
    }

}
