package com.nowbox.nowbox_api.modules.cargo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cargo.dto.CargoCreateDTO;
import com.nowbox.nowbox_api.modules.cargo.dto.CargoResponseDTO;
import com.nowbox.nowbox_api.modules.cargo.service.CargoService;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
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

@WebMvcTest(CargoController.class)
@AutoConfigureMockMvc(addFilters = false)
class CargoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CargoService cargoService;

    @Test
    @DisplayName("Should list cargos with status 200")
    void listAll() throws Exception {
        // Mock para simular resposta do Service
        UnidadeEntity unidade = UnidadeEntity.builder().id(UUID.randomUUID()).nome("Unidade").cidade("Cidade Test").build();
        CargoResponseDTO dto = CargoResponseDTO.builder().id(UUID.randomUUID()).nome("Cargo Test").unidade(unidade).build();
        Page<CargoResponseDTO> paginaMock = new PageImpl<>(List.of(dto));

        // Quando chamar listAllByFilter ele retorna o mock paginaMock
        when(cargoService.listAllByFilter(any(), any())).thenReturn(paginaMock);

        // chama o endpoint GET /v1/cargo
        mockMvc.perform(get("/v1/cargo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Cargo Test"));

        // verifica se o service foi chamado
        verify(cargoService).listAllByFilter(any(), any());
    }

    @Test
    @DisplayName("Should return cargo with status 200 when id exists")
    void findCase1() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Service
        UnidadeEntity unidade = UnidadeEntity.builder().id(UUID.randomUUID()).nome("Unidade").cidade("Cidade Test").build();
        CargoResponseDTO dto = CargoResponseDTO.builder().id(id).nome("Cargo Test").unidade(unidade).build();

        // Quando chamar find ele retorna o mock dto
        when(cargoService.find(any(), eq(id))).thenReturn(dto);

        // chama o endpoint GET /v1/cargo/{id}
        mockMvc.perform(get("/v1/cargo/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Cargo Test"));

        // verifica se o service foi chamado com o id correto
        verify(cargoService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when id does not exist")
    void findCase2() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular que o service lanca excecao pois o cargo nao existe
        when(cargoService.find(any(), eq(id))).thenThrow(new NaoEncontradoException("Cargo não encontrado"));

        // chama o endpoint GET /v1/cargo/{id} e verifica se retorna 404
        mockMvc.perform(get("/v1/cargo/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(cargoService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should create cargo with status 201")
    void createCase1() throws Exception {
        // dados para criacao
        UUID idUnidade = UUID.randomUUID();
        CargoCreateDTO cargo = CargoCreateDTO.builder().idUnidade(idUnidade).nome("Cargo Test").build();

        // Mock para simular resposta do Service
        UUID id = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade").cidade("Cidade Test").build();
        CargoResponseDTO dtoSalvo = CargoResponseDTO.builder().id(id).nome("Cargo Test").unidade(unidade).build();

        // Quando chamar create ele retorna o mock dtoSalvo
        when(cargoService.create(any(CargoCreateDTO.class))).thenReturn(dtoSalvo);

        // chama o endpoint POST /v1/cargo
        mockMvc.perform(post("/v1/cargo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cargo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Cargo Test"));

        // verifica se o service foi chamado com os dados corretos
        verify(cargoService).create(argThat(c -> c.getIdUnidade().equals(idUnidade) && c.getNome().equals("Cargo Test")));
    }

    @Test
    @DisplayName("Should return status 404 when creating cargo with a unidade that does not exist")
    void createCase2() throws Exception {
        // dados para criacao com unidade inexistente
        CargoCreateDTO cargo = CargoCreateDTO.builder().idUnidade(UUID.randomUUID()).nome("Cargo Test").build();

        // Mock para simular que o service lanca excecao pois a unidade nao existe
        when(cargoService.create(any(CargoCreateDTO.class))).thenThrow(new NaoEncontradoException("Unidade inválida"));

        // chama o endpoint POST /v1/cargo e verifica se retorna 404
        mockMvc.perform(post("/v1/cargo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cargo)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com os dados corretos
        verify(cargoService).create(any(CargoCreateDTO.class));
    }

    @Test
    @DisplayName("Should update cargo with status 200 when id exists")
    void updateCase1() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UUID idUnidade = UUID.randomUUID();
        CargoCreateDTO cargo = CargoCreateDTO.builder().idUnidade(idUnidade).nome("Cargo Test").build();

        // Mock para simular resposta do Service
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade").cidade("Cidade Test").build();
        CargoResponseDTO dtoAtualizado = CargoResponseDTO.builder().id(id).nome("Cargo Test").unidade(unidade).build();
        when(cargoService.update(any(CargoCreateDTO.class), eq(id))).thenReturn(dtoAtualizado);

        // chama o endpoint PUT /v1/cargo/{id}
        mockMvc.perform(put("/v1/cargo/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cargo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Cargo Test"));

        // verifica se o service foi chamado com o id e os dados corretos
        verify(cargoService).update(argThat(c -> c.getNome().equals("Cargo Test")), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when updating a cargo that does not exist")
    void updateCase2() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        CargoCreateDTO cargo = CargoCreateDTO.builder().idUnidade(UUID.randomUUID()).nome("Cargo Test").build();

        // Mock para simular que o service lanca excecao pois o cargo nao existe
        when(cargoService.update(any(CargoCreateDTO.class), eq(id))).thenThrow(new NaoEncontradoException("Cargo não encontrado"));

        // chama o endpoint PUT /v1/cargo/{id} e verifica se retorna 404
        mockMvc.perform(put("/v1/cargo/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cargo)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(cargoService).update(any(CargoCreateDTO.class), eq(id));
    }

    @Test
    @DisplayName("Should delete cargo with status 204")
    void deleteCase1() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service exclui sem lancar excecao
        doNothing().when(cargoService).delete(id);

        // chama o endpoint DELETE /v1/cargo/{id}
        mockMvc.perform(delete("/v1/cargo/{id}", id))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id correto
        verify(cargoService).delete(id);
    }
}
