package com.nowbox.nowbox_api.modules.usuario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.usuario.dto.UsuarioCreateDTO;
import com.nowbox.nowbox_api.modules.usuario.dto.UsuarioResponseDTO;
import com.nowbox.nowbox_api.modules.usuario.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Should list usuarios with status 200")
    void listAll() throws Exception {
        // Mock para simular resposta do Service
        UsuarioResponseDTO dto = UsuarioResponseDTO.builder().id(UUID.randomUUID()).nome("Usuario Test").email("usuario@test.com").build();
        Page<UsuarioResponseDTO> paginaMock = new PageImpl<>(List.of(dto));

        // Quando chamar listAllByFilter ele retorna o mock paginaMock
        when(usuarioService.listAllByFilter(any(), any())).thenReturn(paginaMock);

        // chama o endpoint GET /v1/usuario
        mockMvc.perform(get("/v1/usuario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Usuario Test"))
                .andExpect(jsonPath("$.content[0].email").value("usuario@test.com"));

        // verifica se o service foi chamado
        verify(usuarioService).listAllByFilter(any(), any());
    }

    @Test
    @DisplayName("Should return usuario with status 200 when id exists")
    void findCase1() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Service
        UsuarioResponseDTO dto = UsuarioResponseDTO.builder().id(id).nome("Usuario Test").email("usuario@test.com").build();

        // Quando chamar find ele retorna o mock dto
        when(usuarioService.find(any(), eq(id))).thenReturn(dto);

        // chama o endpoint GET /v1/usuario/{id}
        mockMvc.perform(get("/v1/usuario/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Usuario Test"));

        // verifica se o service foi chamado com o id correto
        verify(usuarioService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when id does not exist")
    void findCase2() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular que o service lanca excecao pois o usuario nao existe
        when(usuarioService.find(any(), eq(id))).thenThrow(new NaoEncontradoException("Usuário não encontrado"));

        // chama o endpoint GET /v1/usuario/{id} e verifica se retorna 404
        mockMvc.perform(get("/v1/usuario/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(usuarioService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should create usuario with status 201")
    void createCase1() throws Exception {
        // dados para criacao
        UsuarioCreateDTO usuario = UsuarioCreateDTO.builder().nome("Usuario Test").email("usuario@test.com").cpf("12345678901").sexo("M").senha("senha123").build();

        // Mock para simular resposta do Service
        UUID id = UUID.randomUUID();
        UsuarioResponseDTO dtoSalvo = UsuarioResponseDTO.builder().id(id).nome("Usuario Test").email("usuario@test.com").build();

        // Quando chamar create ele retorna o mock dtoSalvo
        when(usuarioService.create(any(UsuarioCreateDTO.class))).thenReturn(dtoSalvo);

        // chama o endpoint POST /v1/usuario
        mockMvc.perform(post("/v1/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Usuario Test"));

        // verifica se o service foi chamado com os dados corretos
        verify(usuarioService).create(argThat(u -> u.getNome().equals("Usuario Test") && u.getEmail().equals("usuario@test.com") && u.getCpf().equals("12345678901")));
    }

    @Test
    @DisplayName("Should update usuario with status 200 when id exists")
    void updateCase1() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UsuarioCreateDTO usuario = UsuarioCreateDTO.builder().nome("Usuario Test").email("usuario@test.com").cpf("12345678901").sexo("M").senha("senha123").build();

        // Mock para simular resposta do Service
        UsuarioResponseDTO dtoAtualizado = UsuarioResponseDTO.builder().id(id).nome("Usuario Test").email("usuario@test.com").build();
        when(usuarioService.update(any(UsuarioCreateDTO.class), eq(id))).thenReturn(dtoAtualizado);

        // chama o endpoint PUT /v1/usuario/{id}
        mockMvc.perform(put("/v1/usuario/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Usuario Test"));

        // verifica se o service foi chamado com o id e os dados corretos
        verify(usuarioService).update(argThat(u -> u.getNome().equals("Usuario Test") && u.getEmail().equals("usuario@test.com")), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when updating a usuario that does not exist")
    void updateCase2() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UsuarioCreateDTO usuario = UsuarioCreateDTO.builder().nome("Usuario Test").build();

        // Mock para simular que o service lanca excecao pois o usuario nao existe
        when(usuarioService.update(any(UsuarioCreateDTO.class), eq(id))).thenThrow(new NaoEncontradoException("Usuário não encontrado"));

        // chama o endpoint PUT /v1/usuario/{id} e verifica se retorna 404
        mockMvc.perform(put("/v1/usuario/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(usuarioService).update(any(UsuarioCreateDTO.class), eq(id));
    }

    @Test
    @DisplayName("Should delete usuario with status 204")
    void deleteCase1() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service exclui sem lancar excecao
        doNothing().when(usuarioService).delete(id);

        // chama o endpoint DELETE /v1/usuario/{id}
        mockMvc.perform(delete("/v1/usuario/{id}", id))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id correto
        verify(usuarioService).delete(id);
    }
}
