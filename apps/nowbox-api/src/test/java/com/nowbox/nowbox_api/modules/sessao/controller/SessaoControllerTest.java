package com.nowbox.nowbox_api.modules.sessao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.sessao.dto.SessaoCreateDTO;
import com.nowbox.nowbox_api.modules.sessao.dto.SessaoResponseDTO;
import com.nowbox.nowbox_api.modules.sessao.service.SessaoService;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessaoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SessaoService sessaoService;

    @Test
    @DisplayName("Should list sessions with status 200")
    void listAll() throws Exception {
        // Mock para simular resposta do Service
        SessaoResponseDTO dto = SessaoResponseDTO.builder().id(UUID.randomUUID()).nome("Session Test").rota("/test").build();
        Page<SessaoResponseDTO> paginaMock = new PageImpl<>(List.of(dto));

        // Quando chamar listAll ele retorna o mock paginaMock
        when(sessaoService.listAll(any(), any())).thenReturn(paginaMock);

        // chama o endpoint GET /v1/sessao
        mockMvc.perform(get("/v1/sessao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Session Test"))
                .andExpect(jsonPath("$.content[0].rota").value("/test"));

        // verifica se o service foi chamado
        verify(sessaoService).listAll(any(), any());
    }

    @Test
    @DisplayName("Should return session with status 200 when id exists")
    void listByIdCase1() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Service
        SessaoResponseDTO dto = SessaoResponseDTO.builder().id(id).nome("Session Test").rota("/test").build();

        // Quando chamar listById ele retorna o mock dto
        when(sessaoService.listById(id)).thenReturn(Optional.of(dto));

        // chama o endpoint GET /v1/sessao/{id}
        mockMvc.perform(get("/v1/sessao/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Session Test"));

        // verifica se o service foi chamado com o id correto
        verify(sessaoService).listById(id);
    }

    @Test
    @DisplayName("Should return status 404 when id does not exist")
    void listByIdCase2() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Quando chamar listById ele retorna vazio
        when(sessaoService.listById(id)).thenReturn(Optional.empty());

        // chama o endpoint GET /v1/sessao/{id} e verifica se retorna 404
        mockMvc.perform(get("/v1/sessao/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(sessaoService).listById(id);
    }

    @Test
    @DisplayName("Should create session with status 201")
    void createCase1() throws Exception {
        // dados para criacao
        SessaoCreateDTO sessao = SessaoCreateDTO.builder().nome("Session Test").rota("/test").build();

        // Mock para simular resposta do Service
        UUID id = UUID.randomUUID();
        SessaoResponseDTO dtoSalvo = SessaoResponseDTO.builder().id(id).nome("Session Test").rota("/test").build();

        // Quando chamar save ele retorna o mock dtoSalvo
        when(sessaoService.save(any(SessaoCreateDTO.class))).thenReturn(dtoSalvo);

        // chama o endpoint POST /v1/sessao
        mockMvc.perform(post("/v1/sessao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessao)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Session Test"));

        // verifica se o service foi chamado com os dados corretos
        verify(sessaoService).save(any(SessaoCreateDTO.class));
    }

    @Test
    @DisplayName("Should return status 400 when creating session with blank fields")
    void createCase2() throws Exception {
        // dados invalidos para criacao com nome e rota em branco
        SessaoCreateDTO sessao = SessaoCreateDTO.builder().nome("").rota("").build();

        // chama o endpoint POST /v1/sessao e verifica se retorna 400
        mockMvc.perform(post("/v1/sessao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessao)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should update session with status 204 when id exists")
    void updateCase1() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        SessaoCreateDTO sessao = SessaoCreateDTO.builder().nome("Session Test").rota("/test").build();

        // Mock para simular que o service atualiza sem lancar excecao
        doNothing().when(sessaoService).update(any(UUID.class), any(SessaoCreateDTO.class));

        // chama o endpoint PUT /v1/sessao/{id}
        mockMvc.perform(put("/v1/sessao/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessao)))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(sessaoService).update(eq(id), argThat(s -> s.getNome().equals("Session Test") && s.getRota().equals("/test")));
    }

    @Test
    @DisplayName("Should return status 404 when updating a session that does not exist")
    void updateCase2() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        SessaoCreateDTO sessao = SessaoCreateDTO.builder().nome("Session Test").rota("/test").build();

        // Mock para simular que o service lanca excecao pois a sessao nao existe
        doThrow(new NaoEncontradoException("Sessao não encontrada.")).when(sessaoService).update(any(UUID.class), any(SessaoCreateDTO.class));

        // chama o endpoint PUT /v1/sessao/{id} e verifica se retorna 404
        mockMvc.perform(put("/v1/sessao/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessao)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(sessaoService).update(eq(id), argThat(s -> s.getNome().equals("Session Test") && s.getRota().equals("/test")));
    }

    @Test
    @DisplayName("Should delete session with status 204 when id exists")
    void deleteCase1() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service exclui sem lancar excecao
        doNothing().when(sessaoService).delete(id);

        // chama o endpoint DELETE /v1/sessao/{id}
        mockMvc.perform(delete("/v1/sessao/{id}", id))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id correto
        verify(sessaoService).delete(id);
    }

    @Test
    @DisplayName("Should return status 404 when deleting a session that does not exist")
    void deleteCase2() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service lanca excecao pois a sessao nao existe
        doThrow(new NaoEncontradoException("Sessao não encontrada.")).when(sessaoService).delete(id);

        // chama o endpoint DELETE /v1/sessao/{id} e verifica se retorna 404
        mockMvc.perform(delete("/v1/sessao/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(sessaoService).delete(id);
    }
}