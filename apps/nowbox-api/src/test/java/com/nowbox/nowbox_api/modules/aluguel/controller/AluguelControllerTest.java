package com.nowbox.nowbox_api.modules.aluguel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbox.nowbox_api.common.exception.ConflitoException;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelCreateDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelResponseDTO;
import com.nowbox.nowbox_api.modules.aluguel.service.AluguelService;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
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

import java.math.BigDecimal;
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

@WebMvcTest(AluguelController.class)
@AutoConfigureMockMvc(addFilters = false)
class AluguelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AluguelService aluguelService;

    @Test
    @DisplayName("Should list alugueis with status 200")
    void listAll() throws Exception {
        // Mock para simular resposta do Service
        BoxEntity box = BoxEntity.builder().id(UUID.randomUUID()).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(UUID.randomUUID()).nome("Cliente").build();
        AluguelResponseDTO dto = AluguelResponseDTO.builder().id(UUID.randomUUID()).box(box).cliente(cliente).valor(BigDecimal.valueOf(150.00)).build();
        Page<AluguelResponseDTO> paginaMock = new PageImpl<>(List.of(dto));

        // Quando chamar listAllByFilter ele retorna o mock paginaMock
        when(aluguelService.listAllByFilter(any(), any())).thenReturn(paginaMock);

        // chama o endpoint GET /v1/aluguel
        mockMvc.perform(get("/v1/aluguel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].box.numero").value("101"))
                .andExpect(jsonPath("$.content[0].cliente.nome").value("Cliente"));

        // verifica se o service foi chamado
        verify(aluguelService).listAllByFilter(any(), any());
    }

    @Test
    @DisplayName("Should return aluguel with status 200 when id exists")
    void findCase1() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Service
        BoxEntity box = BoxEntity.builder().id(UUID.randomUUID()).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(UUID.randomUUID()).nome("Cliente").build();
        AluguelResponseDTO dto = AluguelResponseDTO.builder().id(id).box(box).cliente(cliente).build();

        // Quando chamar find ele retorna o mock dto
        when(aluguelService.find(any(), eq(id))).thenReturn(dto);

        // chama o endpoint GET /v1/aluguel/{id}
        mockMvc.perform(get("/v1/aluguel/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.cliente.nome").value("Cliente"));

        // verifica se o service foi chamado com o id correto
        verify(aluguelService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when id does not exist")
    void findCase2() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular que o service lanca excecao pois o aluguel nao existe
        when(aluguelService.find(any(), eq(id))).thenThrow(new NaoEncontradoException("Aluguel não encontrado"));

        // chama o endpoint GET /v1/aluguel/{id} e verifica se retorna 404
        mockMvc.perform(get("/v1/aluguel/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(aluguelService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should create aluguel with status 201")
    void createCase1() throws Exception {
        // dados para criacao
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).valor(BigDecimal.valueOf(150.00)).build();

        // Mock para simular resposta do Service
        UUID id = UUID.randomUUID();
        BoxEntity box = BoxEntity.builder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();
        AluguelResponseDTO dtoSalvo = AluguelResponseDTO.builder().id(id).box(box).cliente(cliente).valor(BigDecimal.valueOf(150.00)).build();

        // Quando chamar create ele retorna o mock dtoSalvo
        when(aluguelService.create(any(AluguelCreateDTO.class))).thenReturn(dtoSalvo);

        // chama o endpoint POST /v1/aluguel
        mockMvc.perform(post("/v1/aluguel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aluguel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.cliente.nome").value("Cliente"));

        // verifica se o service foi chamado com os dados corretos
        verify(aluguelService).create(argThat(a -> a.getIdBox().equals(idBox) && a.getIdCliente().equals(idCliente)));
    }

    @Test
    @DisplayName("Should return status 404 when creating aluguel with a box that does not exist")
    void createCase2() throws Exception {
        // dados para criacao com box inexistente
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(UUID.randomUUID()).idCliente(UUID.randomUUID()).build();

        // Mock para simular que o service lanca excecao pois o box nao existe
        when(aluguelService.create(any(AluguelCreateDTO.class))).thenThrow(new NaoEncontradoException("Box inválido"));

        // chama o endpoint POST /v1/aluguel e verifica se retorna 404
        mockMvc.perform(post("/v1/aluguel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aluguel)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com os dados corretos
        verify(aluguelService).create(any(AluguelCreateDTO.class));
    }

    @Test
    @DisplayName("Should return status 409 when creating an aluguel for a box that already has an active one")
    void createCase3() throws Exception {
        // dados para criacao de um aluguel ativo
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(UUID.randomUUID()).idCliente(UUID.randomUUID()).status(true).build();

        // Mock para simular que o service lanca excecao pois o box ja tem um aluguel ativo
        when(aluguelService.create(any(AluguelCreateDTO.class))).thenThrow(new ConflitoException("O box já possui um aluguel ativo"));

        // chama o endpoint POST /v1/aluguel e verifica se retorna 409
        mockMvc.perform(post("/v1/aluguel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aluguel)))
                .andExpect(status().isConflict());

        // verifica se o service foi chamado com os dados corretos
        verify(aluguelService).create(any(AluguelCreateDTO.class));
    }

    @Test
    @DisplayName("Should return status 409 when updating an aluguel for a box that already has another active one")
    void updateCase3() throws Exception {
        // id e dados para atualizacao de um aluguel ativo
        UUID id = UUID.randomUUID();
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(UUID.randomUUID()).idCliente(UUID.randomUUID()).status(true).build();

        // Mock para simular que o service lanca excecao pois o box ja tem outro aluguel ativo
        when(aluguelService.update(any(AluguelCreateDTO.class), eq(id))).thenThrow(new ConflitoException("O box já possui um aluguel ativo"));

        // chama o endpoint PUT /v1/aluguel/{id} e verifica se retorna 409
        mockMvc.perform(put("/v1/aluguel/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aluguel)))
                .andExpect(status().isConflict());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(aluguelService).update(any(AluguelCreateDTO.class), eq(id));
    }

    @Test
    @DisplayName("Should update aluguel with status 200 when id exists")
    void updateCase1() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(idBox).idCliente(idCliente).build();

        // Mock para simular resposta do Service
        BoxEntity box = BoxEntity.builder().id(idBox).numero("101").build();
        ClienteEntity cliente = ClienteEntity.builder().id(idCliente).nome("Cliente").build();
        AluguelResponseDTO dtoAtualizado = AluguelResponseDTO.builder().id(id).box(box).cliente(cliente).build();
        when(aluguelService.update(any(AluguelCreateDTO.class), eq(id))).thenReturn(dtoAtualizado);

        // chama o endpoint PUT /v1/aluguel/{id}
        mockMvc.perform(put("/v1/aluguel/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aluguel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.cliente.nome").value("Cliente"));

        // verifica se o service foi chamado com o id e os dados corretos
        verify(aluguelService).update(argThat(a -> a.getIdBox().equals(idBox) && a.getIdCliente().equals(idCliente)), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when updating an aluguel that does not exist")
    void updateCase2() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        AluguelCreateDTO aluguel = AluguelCreateDTO.builder().idBox(UUID.randomUUID()).idCliente(UUID.randomUUID()).build();

        // Mock para simular que o service lanca excecao pois o aluguel nao existe
        when(aluguelService.update(any(AluguelCreateDTO.class), eq(id))).thenThrow(new NaoEncontradoException("Aluguel não encontrado"));

        // chama o endpoint PUT /v1/aluguel/{id} e verifica se retorna 404
        mockMvc.perform(put("/v1/aluguel/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aluguel)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(aluguelService).update(any(AluguelCreateDTO.class), eq(id));
    }

    @Test
    @DisplayName("Should delete aluguel with status 204")
    void deleteCase1() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service exclui sem lancar excecao
        doNothing().when(aluguelService).delete(id);

        // chama o endpoint DELETE /v1/aluguel/{id}
        mockMvc.perform(delete("/v1/aluguel/{id}", id))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id correto
        verify(aluguelService).delete(id);
    }
}
