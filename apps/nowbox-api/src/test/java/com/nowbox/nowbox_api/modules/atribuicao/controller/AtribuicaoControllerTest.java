package com.nowbox.nowbox_api.modules.atribuicao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.atribuicao.dto.AtribuicaoCreateDTO;
import com.nowbox.nowbox_api.modules.atribuicao.dto.AtribuicaoResponseDTO;
import com.nowbox.nowbox_api.modules.atribuicao.service.AtribuicaoService;
import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.usuario.entity.UsuarioEntity;
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

@WebMvcTest(AtribuicaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class AtribuicaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AtribuicaoService atribuicaoService;

    @Test
    @DisplayName("Should list atribuicoes with status 200")
    void listAll() throws Exception {
        // Mock para simular resposta do Service
        UsuarioEntity usuario = UsuarioEntity.builder().id(UUID.randomUUID()).nome("Usuario").build();
        CargoEntity cargo = CargoEntity.builder().id(UUID.randomUUID()).nome("Cargo").build();
        AtribuicaoResponseDTO dto = AtribuicaoResponseDTO.builder().id(UUID.randomUUID()).usuario(usuario).cargo(cargo).build();
        Page<AtribuicaoResponseDTO> paginaMock = new PageImpl<>(List.of(dto));

        // Quando chamar listAllByFilter ele retorna o mock paginaMock
        when(atribuicaoService.listAllByFilter(any(), any())).thenReturn(paginaMock);

        // chama o endpoint GET /v1/atribuicao
        mockMvc.perform(get("/v1/atribuicao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].usuario.nome").value("Usuario"))
                .andExpect(jsonPath("$.content[0].cargo.nome").value("Cargo"));

        // verifica se o service foi chamado
        verify(atribuicaoService).listAllByFilter(any(), any());
    }

    @Test
    @DisplayName("Should return atribuicao with status 200 when id exists")
    void findCase1() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Service
        UsuarioEntity usuario = UsuarioEntity.builder().id(UUID.randomUUID()).nome("Usuario").build();
        CargoEntity cargo = CargoEntity.builder().id(UUID.randomUUID()).nome("Cargo").build();
        AtribuicaoResponseDTO dto = AtribuicaoResponseDTO.builder().id(id).usuario(usuario).cargo(cargo).build();

        // Quando chamar find ele retorna o mock dto
        when(atribuicaoService.find(any(), eq(id))).thenReturn(dto);

        // chama o endpoint GET /v1/atribuicao/{id}
        mockMvc.perform(get("/v1/atribuicao/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.usuario.nome").value("Usuario"));

        // verifica se o service foi chamado com o id correto
        verify(atribuicaoService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when id does not exist")
    void findCase2() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular que o service lanca excecao pois a atribuicao nao existe
        when(atribuicaoService.find(any(), eq(id))).thenThrow(new NaoEncontradoException("Atribuição não encontrada"));

        // chama o endpoint GET /v1/atribuicao/{id} e verifica se retorna 404
        mockMvc.perform(get("/v1/atribuicao/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(atribuicaoService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should create atribuicao with status 201")
    void createCase1() throws Exception {
        // dados para criacao
        UUID idUsuario = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();
        AtribuicaoCreateDTO atribuicao = AtribuicaoCreateDTO.builder().idUsuario(idUsuario).idCargo(idCargo).build();

        // Mock para simular resposta do Service
        UUID id = UUID.randomUUID();
        UsuarioEntity usuario = UsuarioEntity.builder().id(idUsuario).nome("Usuario").build();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        AtribuicaoResponseDTO dtoSalvo = AtribuicaoResponseDTO.builder().id(id).usuario(usuario).cargo(cargo).build();

        // Quando chamar create ele retorna o mock dtoSalvo
        when(atribuicaoService.create(any(AtribuicaoCreateDTO.class))).thenReturn(dtoSalvo);

        // chama o endpoint POST /v1/atribuicao
        mockMvc.perform(post("/v1/atribuicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atribuicao)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.usuario.nome").value("Usuario"));

        // verifica se o service foi chamado com os dados corretos
        verify(atribuicaoService).create(argThat(a -> a.getIdUsuario().equals(idUsuario) && a.getIdCargo().equals(idCargo)));
    }

    @Test
    @DisplayName("Should return status 404 when creating atribuicao with a usuario that does not exist")
    void createCase2() throws Exception {
        // dados para criacao com usuario inexistente
        AtribuicaoCreateDTO atribuicao = AtribuicaoCreateDTO.builder().idUsuario(UUID.randomUUID()).idCargo(UUID.randomUUID()).build();

        // Mock para simular que o service lanca excecao pois o usuario nao existe
        when(atribuicaoService.create(any(AtribuicaoCreateDTO.class))).thenThrow(new NaoEncontradoException("Usuário inválido"));

        // chama o endpoint POST /v1/atribuicao e verifica se retorna 404
        mockMvc.perform(post("/v1/atribuicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atribuicao)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com os dados corretos
        verify(atribuicaoService).create(any(AtribuicaoCreateDTO.class));
    }

    @Test
    @DisplayName("Should update atribuicao with status 200 when id exists")
    void updateCase1() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UUID idUsuario = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();
        AtribuicaoCreateDTO atribuicao = AtribuicaoCreateDTO.builder().idUsuario(idUsuario).idCargo(idCargo).build();

        // Mock para simular resposta do Service
        UsuarioEntity usuario = UsuarioEntity.builder().id(idUsuario).nome("Usuario").build();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        AtribuicaoResponseDTO dtoAtualizado = AtribuicaoResponseDTO.builder().id(id).usuario(usuario).cargo(cargo).build();
        when(atribuicaoService.update(any(AtribuicaoCreateDTO.class), eq(id))).thenReturn(dtoAtualizado);

        // chama o endpoint PUT /v1/atribuicao/{id}
        mockMvc.perform(put("/v1/atribuicao/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atribuicao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.usuario.nome").value("Usuario"));

        // verifica se o service foi chamado com o id e os dados corretos
        verify(atribuicaoService).update(argThat(a -> a.getIdUsuario().equals(idUsuario) && a.getIdCargo().equals(idCargo)), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when updating an atribuicao that does not exist")
    void updateCase2() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        AtribuicaoCreateDTO atribuicao = AtribuicaoCreateDTO.builder().idUsuario(UUID.randomUUID()).idCargo(UUID.randomUUID()).build();

        // Mock para simular que o service lanca excecao pois a atribuicao nao existe
        when(atribuicaoService.update(any(AtribuicaoCreateDTO.class), eq(id))).thenThrow(new NaoEncontradoException("Atribuição não encontrada"));

        // chama o endpoint PUT /v1/atribuicao/{id} e verifica se retorna 404
        mockMvc.perform(put("/v1/atribuicao/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atribuicao)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(atribuicaoService).update(any(AtribuicaoCreateDTO.class), eq(id));
    }

    @Test
    @DisplayName("Should delete atribuicao with status 204")
    void deleteCase1() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service exclui sem lancar excecao
        doNothing().when(atribuicaoService).delete(id);

        // chama o endpoint DELETE /v1/atribuicao/{id}
        mockMvc.perform(delete("/v1/atribuicao/{id}", id))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id correto
        verify(atribuicaoService).delete(id);
    }
}
