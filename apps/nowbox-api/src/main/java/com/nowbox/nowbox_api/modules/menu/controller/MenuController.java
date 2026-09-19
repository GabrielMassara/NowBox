package com.nowbox.nowbox_api.modules.menu.controller;

import com.nowbox.nowbox_api.modules.menu.dto.MenuSessaoResponseDTO;
import com.nowbox.nowbox_api.modules.menu.service.MenuService;
import com.nowbox.nowbox_api.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MenuSessaoResponseDTO> listMenu(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return menuService.listByUsuario(usuario.getId());
    }

}
