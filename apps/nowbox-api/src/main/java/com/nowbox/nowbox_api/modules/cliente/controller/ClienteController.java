package com.nowbox.nowbox_api.modules.cliente.controller;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteCreateDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteFilterDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteResponseDTO;
import com.nowbox.nowbox_api.modules.cliente.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_CONSULTAR')")
    public Page<ClienteResponseDTO> listAll(Pageable pageable, @ModelAttribute ClienteFilterDTO filtro) {
        return clienteService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_CONSULTAR')")
    public ClienteResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return clienteService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_CADASTRAR')")
    public ClienteResponseDTO create(@RequestBody ClienteCreateDTO cliente) throws NaoEncontradoException {
        return clienteService.create(cliente);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_ATUALIZAR')")
    public ClienteResponseDTO update(@RequestBody ClienteCreateDTO cliente, @PathVariable UUID id) {
        return clienteService.update(cliente, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MOD_CLIENTE_OPE_EXCLUIR')")
    public void delete(@PathVariable UUID id) {
        clienteService.delete(id);
    }

}
