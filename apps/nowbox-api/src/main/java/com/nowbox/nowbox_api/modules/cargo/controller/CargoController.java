package com.nowbox.nowbox_api.modules.cargo.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cargo.dto.CargoCreateDTO;
import com.nowbox.nowbox_api.modules.cargo.dto.CargoFilterDTO;
import com.nowbox.nowbox_api.modules.cargo.dto.CargoResponseDTO;
import com.nowbox.nowbox_api.modules.cargo.service.CargoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/cargo")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    private Page<CargoResponseDTO> listAll(Pageable pageable, @ModelAttribute CargoFilterDTO filtro) {
        return cargoService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private CargoResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return cargoService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private CargoResponseDTO create(@RequestBody CargoCreateDTO cargo) throws NaoEncontradoException {
        return cargoService.create(cargo);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private CargoResponseDTO update(@RequestBody CargoCreateDTO cargo, @PathVariable UUID id) {
        return cargoService.update(cargo, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private void delete(@PathVariable UUID id) {
        cargoService.delete(id);
    }

}
