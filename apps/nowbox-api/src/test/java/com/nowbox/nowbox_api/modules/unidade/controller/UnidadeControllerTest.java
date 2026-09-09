package com.nowbox.nowbox_api.modules.unidade.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.unidade.dto.UnidadeCreateDTO;
import com.nowbox.nowbox_api.modules.unidade.dto.UnidadeResponseDTO;
import com.nowbox.nowbox_api.modules.unidade.service.UnidadeService;
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

@WebMvcTest(UnidadeController.class)
@AutoConfigureMockMvc(addFilters = false)
class UnidadeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UnidadeService unidadeService;

    @Test
    @DisplayName("Should list unidades with status 200")
    void listAll() throws Exception {
        // Mock para simular resposta do Service
        EstadoEntity estado = EstadoEntity.builder().id(UUID.randomUUID()).nome("Estado").uf("ES").build();
        UnidadeResponseDTO dto = UnidadeResponseDTO.builder().id(UUID.randomUUID()).nome("Unidade Test").cidade("Cidade Test").estado(estado).build();
        Page<UnidadeResponseDTO> paginaMock = new PageImpl<>(List.of(dto));

        // Quando chamar listAllByFilter ele retorna o mock paginaMock
        when(unidadeService.listAllByFilter(any(), any())).thenReturn(paginaMock);

        // chama o endpoint GET /v1/unidade
        mockMvc.perform(get("/v1/unidade"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Unidade Test"))
                .andExpect(jsonPath("$.content[0].cidade").value("Cidade Test"));

        // verifica se o service foi chamado
        verify(unidadeService).listAllByFilter(any(), any());
    }

    @Test
    @DisplayName("Should return unidade with status 200 when id exists")
    void findCase1() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Service
        EstadoEntity estado = EstadoEntity.builder().id(UUID.randomUUID()).nome("Estado").uf("ES").build();
        UnidadeResponseDTO dto = UnidadeResponseDTO.builder().id(id).nome("Unidade Test").cidade("Cidade Test").estado(estado).build();

        // Quando chamar find ele retorna o mock dto
        when(unidadeService.find(any(), eq(id))).thenReturn(dto);

        // chama o endpoint GET /v1/unidade/{id}
        mockMvc.perform(get("/v1/unidade/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Unidade Test"));

        // verifica se o service foi chamado com o id correto
        verify(unidadeService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when id does not exist")
    void findCase2() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular que o service lanca excecao pois a unidade nao existe
        when(unidadeService.find(any(), eq(id))).thenThrow(new NaoEncontradoException("Unidade não encontrada"));

        // chama o endpoint GET /v1/unidade/{id} e verifica se retorna 404
        mockMvc.perform(get("/v1/unidade/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(unidadeService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should create unidade with status 201")
    void createCase1() throws Exception {
        // dados para criacao
        UUID idEstado = UUID.randomUUID();
        UnidadeCreateDTO unidade = UnidadeCreateDTO.builder().idEstado(idEstado).nome("Unidade Test").cidade("Cidade Test").build();

        // Mock para simular resposta do Service
        UUID id = UUID.randomUUID();
        EstadoEntity estado = EstadoEntity.builder().id(idEstado).nome("Estado").uf("ES").build();
        UnidadeResponseDTO dtoSalvo = UnidadeResponseDTO.builder().id(id).nome("Unidade Test").cidade("Cidade Test").estado(estado).build();

        // Quando chamar create ele retorna o mock dtoSalvo
        when(unidadeService.create(any(UnidadeCreateDTO.class))).thenReturn(dtoSalvo);

        // chama o endpoint POST /v1/unidade
        mockMvc.perform(post("/v1/unidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unidade)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Unidade Test"));

        // verifica se o service foi chamado com os dados corretos
        verify(unidadeService).create(argThat(u -> u.getIdEstado().equals(idEstado) && u.getNome().equals("Unidade Test") && u.getCidade().equals("Cidade Test")));
    }

    @Test
    @DisplayName("Should return status 404 when creating unidade with an estado that does not exist")
    void createCase2() throws Exception {
        // dados para criacao com estado inexistente
        UnidadeCreateDTO unidade = UnidadeCreateDTO.builder().idEstado(UUID.randomUUID()).nome("Unidade Test").build();

        // Mock para simular que o service lanca excecao pois o estado nao existe
        when(unidadeService.create(any(UnidadeCreateDTO.class))).thenThrow(new NaoEncontradoException("Estado inválido"));

        // chama o endpoint POST /v1/unidade e verifica se retorna 404
        mockMvc.perform(post("/v1/unidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unidade)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com os dados corretos
        verify(unidadeService).create(any(UnidadeCreateDTO.class));
    }

    @Test
    @DisplayName("Should update unidade with status 200 when id exists")
    void updateCase1() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UUID idEstado = UUID.randomUUID();
        UnidadeCreateDTO unidade = UnidadeCreateDTO.builder().idEstado(idEstado).nome("Unidade Test").cidade("Cidade Test").build();

        // Mock para simular resposta do Service
        EstadoEntity estado = EstadoEntity.builder().id(idEstado).nome("Estado").uf("ES").build();
        UnidadeResponseDTO dtoAtualizado = UnidadeResponseDTO.builder().id(id).nome("Unidade Test").cidade("Cidade Test").estado(estado).build();
        when(unidadeService.update(any(UnidadeCreateDTO.class), eq(id))).thenReturn(dtoAtualizado);

        // chama o endpoint PUT /v1/unidade/{id}
        mockMvc.perform(put("/v1/unidade/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unidade)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Unidade Test"));

        // verifica se o service foi chamado com o id e os dados corretos
        verify(unidadeService).update(argThat(u -> u.getNome().equals("Unidade Test") && u.getCidade().equals("Cidade Test")), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when updating a unidade that does not exist")
    void updateCase2() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UnidadeCreateDTO unidade = UnidadeCreateDTO.builder().idEstado(UUID.randomUUID()).nome("Unidade Test").build();

        // Mock para simular que o service lanca excecao pois a unidade nao existe
        when(unidadeService.update(any(UnidadeCreateDTO.class), eq(id))).thenThrow(new NaoEncontradoException("Unidade não encontrada"));

        // chama o endpoint PUT /v1/unidade/{id} e verifica se retorna 404
        mockMvc.perform(put("/v1/unidade/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unidade)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(unidadeService).update(any(UnidadeCreateDTO.class), eq(id));
    }

    @Test
    @DisplayName("Should delete unidade with status 204")
    void deleteCase1() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service exclui sem lancar excecao
        doNothing().when(unidadeService).delete(id);

        // chama o endpoint DELETE /v1/unidade/{id}
        mockMvc.perform(delete("/v1/unidade/{id}", id))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id correto
        verify(unidadeService).delete(id);
    }
}
