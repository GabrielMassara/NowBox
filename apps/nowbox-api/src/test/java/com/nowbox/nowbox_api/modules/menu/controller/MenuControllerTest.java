package com.nowbox.nowbox_api.modules.menu.controller;

import com.nowbox.nowbox_api.modules.menu.dto.MenuModuloResponseDTO;
import com.nowbox.nowbox_api.modules.menu.dto.MenuSessaoResponseDTO;
import com.nowbox.nowbox_api.modules.menu.service.MenuService;
import com.nowbox.nowbox_api.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
@AutoConfigureMockMvc(addFilters = false)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @AfterEach
    void tearDown() {
        // limpa o contexto de seguranca entre os testes
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should list the menu of the authenticated usuario with status 200")
    void listMenuCase1() throws Exception {
        // usuario autenticado sem permissoes
        UUID idUsuario = UUID.randomUUID();
        UsuarioAutenticado principal = new UsuarioAutenticado(idUsuario, Set.of(), Set.of());

        // Mock para simular resposta do Service
        MenuModuloResponseDTO modulo = MenuModuloResponseDTO.builder().id(UUID.randomUUID()).nome("Modulo Test").rota("/modulo").build();
        MenuSessaoResponseDTO sessao = MenuSessaoResponseDTO.builder()
                .id(UUID.randomUUID()).nome("Sessao Test").rota("/sessao").modulos(List.of(modulo)).build();

        // Quando chamar listByUsuario ele retorna o mock
        when(menuService.listByUsuario(idUsuario)).thenReturn(List.of(sessao));

        // autentica a requisicao como o usuario
        autenticar(principal);

        // chama o endpoint GET /v1/menu
        mockMvc.perform(get("/v1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Sessao Test"))
                .andExpect(jsonPath("$[0].rota").value("/sessao"))
                .andExpect(jsonPath("$[0].modulos[0].nome").value("Modulo Test"))
                .andExpect(jsonPath("$[0].modulos[0].rota").value("/modulo"));

        // verifica se o service foi chamado com o id do usuario
        verify(menuService).listByUsuario(idUsuario);
    }

    @Test
    @DisplayName("Should return empty list with status 200 when the usuario has no permitted modulo")
    void listMenuCase2() throws Exception {
        // usuario autenticado
        UUID idUsuario = UUID.randomUUID();
        UsuarioAutenticado principal = new UsuarioAutenticado(idUsuario, Set.of(), Set.of());

        // Quando chamar listByUsuario ele retorna vazio
        when(menuService.listByUsuario(idUsuario)).thenReturn(List.of());

        // autentica a requisicao como o usuario
        autenticar(principal);

        // chama o endpoint GET /v1/menu
        mockMvc.perform(get("/v1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // verifica se o service foi chamado com o id do usuario
        verify(menuService).listByUsuario(idUsuario);
    }

    // Registra o usuario no SecurityContextHolder
    private void autenticar(UsuarioAutenticado principal) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
