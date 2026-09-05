package com.nowbox.nowbox_api.modules.modulo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.modulo.dto.ModuloCreateDTO;
import com.nowbox.nowbox_api.modules.modulo.dto.ModuloResponseDTO;
import com.nowbox.nowbox_api.modules.modulo.service.ModuloService;
import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
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

@WebMvcTest(ModuloController.class)
@AutoConfigureMockMvc(addFilters = false)
class ModuloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ModuloService moduloService;

    @Test
    @DisplayName("Should list modulos with status 200")
    void listAll() throws Exception {
        // Mock para simular resposta do Service
        SessaoEntity sessao = SessaoEntity.builder().id(UUID.randomUUID()).nome("Sessao").rota("/rotaSessao").build();
        ModuloResponseDTO dto = ModuloResponseDTO.builder().id(UUID.randomUUID()).nome("Modulo Test").rota("/modulo").sessao(sessao).build();
        Page<ModuloResponseDTO> paginaMock = new PageImpl<>(List.of(dto));

        // Quando chamar listAllByFilter ele retorna o mock paginaMock
        when(moduloService.listAllByFilter(any(), any())).thenReturn(paginaMock);

        // chama o endpoint GET /v1/modulo
        mockMvc.perform(get("/v1/modulo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Modulo Test"))
                .andExpect(jsonPath("$.content[0].rota").value("/modulo"));

        // verifica se o service foi chamado
        verify(moduloService).listAllByFilter(any(), any());
    }

    @Test
    @DisplayName("Should return modulo with status 200 when id exists")
    void findCase1() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Service
        SessaoEntity sessao = SessaoEntity.builder().id(UUID.randomUUID()).nome("Sessao").rota("/rotaSessao").build();
        ModuloResponseDTO dto = ModuloResponseDTO.builder().id(id).nome("Modulo Test").rota("/modulo").sessao(sessao).build();

        // Quando chamar find ele retorna o mock dto
        when(moduloService.find(any(), eq(id))).thenReturn(dto);

        // chama o endpoint GET /v1/modulo/{id}
        mockMvc.perform(get("/v1/modulo/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Modulo Test"));

        // verifica se o service foi chamado com o id correto
        verify(moduloService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when id does not exist")
    void findCase2() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular que o service lanca excecao pois o modulo nao existe
        when(moduloService.find(any(), eq(id))).thenThrow(new NaoEncontradoException("Modulo não encontrado"));

        // chama o endpoint GET /v1/modulo/{id} e verifica se retorna 404
        mockMvc.perform(get("/v1/modulo/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(moduloService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should create modulo with status 201")
    void createCase1() throws Exception {
        // dados para criacao
        UUID idSessao = UUID.randomUUID();
        ModuloCreateDTO modulo = ModuloCreateDTO.builder().idSessao(idSessao).nome("Modulo Test").rota("/modulo").build();

        // Mock para simular resposta do Service
        UUID id = UUID.randomUUID();
        SessaoEntity sessao = SessaoEntity.builder().id(idSessao).nome("Sessao").rota("/rotaSessao").build();
        ModuloResponseDTO dtoSalvo = ModuloResponseDTO.builder().id(id).nome("Modulo Test").rota("/modulo").sessao(sessao).build();

        // Quando chamar create ele retorna o mock dtoSalvo
        when(moduloService.create(any(ModuloCreateDTO.class))).thenReturn(dtoSalvo);

        // chama o endpoint POST /v1/modulo
        mockMvc.perform(post("/v1/modulo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modulo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Modulo Test"));

        // verifica se o service foi chamado com os dados corretos
        verify(moduloService).create(argThat(m -> m.getIdSessao().equals(idSessao) && m.getNome().equals("Modulo Test") && m.getRota().equals("/modulo")));
    }

    @Test
    @DisplayName("Should return status 404 when creating modulo with a sessao that does not exist")
    void createCase2() throws Exception {
        // dados para criacao com sessao inexistente
        ModuloCreateDTO modulo = ModuloCreateDTO.builder().idSessao(UUID.randomUUID()).nome("Modulo Test").rota("/modulo").build();

        // Mock para simular que o service lanca excecao pois a sessao nao existe
        when(moduloService.create(any(ModuloCreateDTO.class))).thenThrow(new NaoEncontradoException("Sessão inválida"));

        // chama o endpoint POST /v1/modulo e verifica se retorna 404
        mockMvc.perform(post("/v1/modulo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modulo)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com os dados corretos
        verify(moduloService).create(any(ModuloCreateDTO.class));
    }

    @Test
    @DisplayName("Should update modulo with status 200 when id exists")
    void updateCase1() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UUID idSessao = UUID.randomUUID();
        ModuloCreateDTO modulo = ModuloCreateDTO.builder().idSessao(idSessao).nome("Modulo Test").rota("/modulo").build();

        // Mock para simular resposta do Service
        SessaoEntity sessao = SessaoEntity.builder().id(idSessao).nome("Sessao").rota("/rotaSessao").build();
        ModuloResponseDTO dtoAtualizado = ModuloResponseDTO.builder().id(id).nome("Modulo Test").rota("/modulo").sessao(sessao).build();
        when(moduloService.update(any(ModuloCreateDTO.class), eq(id))).thenReturn(dtoAtualizado);

        // chama o endpoint PUT /v1/modulo/{id}
        mockMvc.perform(put("/v1/modulo/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modulo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Modulo Test"));

        // verifica se o service foi chamado com o id e os dados corretos
        verify(moduloService).update(argThat(m -> m.getNome().equals("Modulo Test") && m.getRota().equals("/modulo")), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when updating a modulo that does not exist")
    void updateCase2() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        ModuloCreateDTO modulo = ModuloCreateDTO.builder().idSessao(UUID.randomUUID()).nome("Modulo Test").rota("/modulo").build();

        // Mock para simular que o service lanca excecao pois o modulo nao existe
        when(moduloService.update(any(ModuloCreateDTO.class), eq(id))).thenThrow(new NaoEncontradoException("Modulo não encontrado"));

        // chama o endpoint PUT /v1/modulo/{id} e verifica se retorna 404
        mockMvc.perform(put("/v1/modulo/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modulo)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(moduloService).update(any(ModuloCreateDTO.class), eq(id));
    }

    @Test
    @DisplayName("Should delete modulo with status 204")
    void deleteCase1() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service exclui sem lancar excecao
        doNothing().when(moduloService).delete(id);

        // chama o endpoint DELETE /v1/modulo/{id}
        mockMvc.perform(delete("/v1/modulo/{id}", id))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id correto
        verify(moduloService).delete(id);
    }
}
