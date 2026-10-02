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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/aluguel")
@RequiredArgsConstructor
public class AluguelController {

    private final AluguelService aluguelService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_ALUGUEL_OPE_CONSULTAR') and @acessoUnidadeService.temAcessoBox(#filtro.idBox)")
    public Page<AluguelResponseDTO> listAll(Pageable pageable, @ModelAttribute AluguelFilterDTO filtro) {
        return aluguelService.listAllByFilter(pageable, filtro);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_ALUGUEL_OPE_CONSULTAR') and @acessoUnidadeService.temAcessoAluguel(#id)")
    public AluguelResponseDTO listAll(Pageable pageable, @PathVariable UUID id) {
        return aluguelService.find(pageable, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MOD_ALUGUEL_OPE_CADASTRAR') and @acessoUnidadeService.temAcessoBox(#aluguel.idBox)")
    public AluguelResponseDTO create(@RequestBody AluguelCreateDTO aluguel) throws NaoEncontradoException {
        return aluguelService.create(aluguel);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_ALUGUEL_OPE_ATUALIZAR') and @acessoUnidadeService.temAcessoAluguel(#id) and @acessoUnidadeService.temAcessoBox(#aluguel.idBox)")
    public AluguelResponseDTO update(@RequestBody AluguelCreateDTO aluguel, @PathVariable UUID id) {
        return aluguelService.update(aluguel, id);
    }

    @PatchMapping("/{id}/encerrar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_ALUGUEL_OPE_ENCERRAR') and @acessoUnidadeService.temAcessoAluguel(#id)")
    public AluguelResponseDTO encerrar(@PathVariable UUID id) {
        return aluguelService.encerrar(id);
    }

    @PatchMapping("/{id}/cancelar-pendencia")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_ALUGUEL_OPE_CANCELAR_PENDENCIA') and @acessoUnidadeService.temAcessoAluguel(#id)")
    public AluguelResponseDTO cancelarPendencia(@PathVariable UUID id) {
        return aluguelService.cancelarPendencia(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MOD_ALUGUEL_OPE_EXCLUIR') and @acessoUnidadeService.temAcessoAluguel(#id)")
    public void delete(@PathVariable UUID id) {
        aluguelService.delete(id);
    }

}
