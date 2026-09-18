package com.nowbox.nowbox_api.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbox.nowbox_api.common.exception.CredenciaisInvalidasException;
import com.nowbox.nowbox_api.modules.auth.dto.LoginRequestDTO;
import com.nowbox.nowbox_api.modules.auth.dto.LoginResponseDTO;
import com.nowbox.nowbox_api.modules.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("Should return token with status 200 when login is valid")
    void loginCase1() throws Exception {
        LoginRequestDTO login = LoginRequestDTO.builder().email("usuario@test.com").senha("senha123").build();

        // Mock para simular resposta do Service
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(LoginResponseDTO.builder().token("token-gerado").build());

        // chama o endpoint POST /v1/auth/login
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-gerado"));

        // verifica se o service foi chamado
        verify(authService).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("Should return status 401 when credentials are invalid")
    void loginCase2() throws Exception {
        LoginRequestDTO login = LoginRequestDTO.builder().email("usuario@test.com").senha("senhaErrada").build();

        // Mock para simular que o service lanca excecao pois as credenciais sao invalidas
        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new CredenciaisInvalidasException("E-mail ou senha inválidos"));

        // chama o endpoint POST /v1/auth/login e verifica se retorna 401
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());

        // verifica se o service foi chamado
        verify(authService).login(any(LoginRequestDTO.class));
    }

}
