package com.nowbox.nowbox_api.modules.cliente.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteCreateDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteResponseDTO;
import com.nowbox.nowbox_api.modules.cliente.service.ClienteService;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
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

import java.time.LocalDate;
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

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private ClienteService clienteService;

    @Test
    @DisplayName("Should list clientes with status 200")
    void listAll() throws Exception {
        // Mock para simular resposta do Service
        EstadoEntity estado = EstadoEntity.builder().id(UUID.randomUUID()).nome("Estado Test").uf("XX").build();
        ClienteResponseDTO dto = ClienteResponseDTO.builder().id(UUID.randomUUID()).nome("Cliente Test").estado(estado).build();
        Page<ClienteResponseDTO> paginaMock = new PageImpl<>(List.of(dto));

        // Quando chamar listAllByFilter ele retorna o mock paginaMock
        when(clienteService.listAllByFilter(any(), any())).thenReturn(paginaMock);

        // chama o endpoint GET /v1/cliente
        mockMvc.perform(get("/v1/cliente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Cliente Test"));

        // verifica se o service foi chamado
        verify(clienteService).listAllByFilter(any(), any());
    }

    @Test
    @DisplayName("Should return cliente with status 200 when id exists")
    void findCase1() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Service
        EstadoEntity estado = EstadoEntity.builder().id(UUID.randomUUID()).nome("Estado Test").uf("XX").build();
        ClienteResponseDTO dto = ClienteResponseDTO.builder().id(id).nome("Cliente Test").estado(estado).build();

        // Quando chamar find ele retorna o mock dto
        when(clienteService.find(any(), eq(id))).thenReturn(dto);

        // chama o endpoint GET /v1/cliente/{id}
        mockMvc.perform(get("/v1/cliente/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Cliente Test"));

        // verifica se o service foi chamado com o id correto
        verify(clienteService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when id does not exist")
    void findCase2() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular que o service lanca excecao pois o cliente nao existe
        when(clienteService.find(any(), eq(id))).thenThrow(new NaoEncontradoException("Cliente não encontrado"));

        // chama o endpoint GET /v1/cliente/{id} e verifica se retorna 404
        mockMvc.perform(get("/v1/cliente/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(clienteService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should create cliente with status 201")
    void createCase1() throws Exception {
        // dados para criacao
        UUID idEstado = UUID.randomUUID();
        ClienteCreateDTO cliente = ClienteCreateDTO.builder().idEstado(idEstado).nome("Cliente Test").nascimento(LocalDate.of(1990, 1, 1)).build();

        // Mock para simular resposta do Service
        UUID id = UUID.randomUUID();
        EstadoEntity estado = EstadoEntity.builder().id(idEstado).nome("Estado Test").uf("XX").build();
        ClienteResponseDTO dtoSalvo = ClienteResponseDTO.builder().id(id).nome("Cliente Test").estado(estado).build();

        // Quando chamar create ele retorna o mock dtoSalvo
        when(clienteService.create(any(ClienteCreateDTO.class))).thenReturn(dtoSalvo);

        // chama o endpoint POST /v1/cliente
        mockMvc.perform(post("/v1/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Cliente Test"));

        // verifica se o service foi chamado com os dados corretos
        verify(clienteService).create(argThat(c -> c.getIdEstado().equals(idEstado) && c.getNome().equals("Cliente Test")));
    }

    @Test
    @DisplayName("Should return status 404 when creating cliente with an estado that does not exist")
    void createCase2() throws Exception {
        // dados para criacao com estado inexistente
        ClienteCreateDTO cliente = ClienteCreateDTO.builder().idEstado(UUID.randomUUID()).nome("Cliente Test").build();

        // Mock para simular que o service lanca excecao pois o estado nao existe
        when(clienteService.create(any(ClienteCreateDTO.class))).thenThrow(new NaoEncontradoException("Estado inválido"));

        // chama o endpoint POST /v1/cliente e verifica se retorna 404
        mockMvc.perform(post("/v1/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com os dados corretos
        verify(clienteService).create(any(ClienteCreateDTO.class));
    }

    @Test
    @DisplayName("Should update cliente with status 200 when id exists")
    void updateCase1() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UUID idEstado = UUID.randomUUID();
        ClienteCreateDTO cliente = ClienteCreateDTO.builder().idEstado(idEstado).nome("Cliente Test").build();

        // Mock para simular resposta do Service
        EstadoEntity estado = EstadoEntity.builder().id(idEstado).nome("Estado Test").uf("XX").build();
        ClienteResponseDTO dtoAtualizado = ClienteResponseDTO.builder().id(id).nome("Cliente Test").estado(estado).build();
        when(clienteService.update(any(ClienteCreateDTO.class), eq(id))).thenReturn(dtoAtualizado);

        // chama o endpoint PUT /v1/cliente/{id}
        mockMvc.perform(put("/v1/cliente/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Cliente Test"));

        // verifica se o service foi chamado com o id e os dados corretos
        verify(clienteService).update(argThat(c -> c.getNome().equals("Cliente Test")), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when updating a cliente that does not exist")
    void updateCase2() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        ClienteCreateDTO cliente = ClienteCreateDTO.builder().idEstado(UUID.randomUUID()).nome("Cliente Test").build();

        // Mock para simular que o service lanca excecao pois o cliente nao existe
        when(clienteService.update(any(ClienteCreateDTO.class), eq(id))).thenThrow(new NaoEncontradoException("Cliente não encontrado"));

        // chama o endpoint PUT /v1/cliente/{id} e verifica se retorna 404
        mockMvc.perform(put("/v1/cliente/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(clienteService).update(any(ClienteCreateDTO.class), eq(id));
    }

    @Test
    @DisplayName("Should delete cliente with status 204")
    void deleteCase1() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service exclui sem lancar excecao
        doNothing().when(clienteService).delete(id);

        // chama o endpoint DELETE /v1/cliente/{id}
        mockMvc.perform(delete("/v1/cliente/{id}", id))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id correto
        verify(clienteService).delete(id);
    }
}
