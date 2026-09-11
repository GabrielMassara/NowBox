package com.nowbox.nowbox_api.modules.permissao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoCreateDTO;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoResponseDTO;
import com.nowbox.nowbox_api.modules.permissao.service.PermissaoService;
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

@WebMvcTest(PermissaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PermissaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PermissaoService permissaoService;

    @Test
    @DisplayName("Should list permissoes with status 200")
    void listAll() throws Exception {
        // Mock para simular resposta do Service
        CargoEntity cargo = CargoEntity.builder().id(UUID.randomUUID()).nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().id(UUID.randomUUID()).nome("Operacao").codigo("OP1").build();
        PermissaoResponseDTO dto = PermissaoResponseDTO.builder().id(UUID.randomUUID()).cargo(cargo).operacao(operacao).build();
        Page<PermissaoResponseDTO> paginaMock = new PageImpl<>(List.of(dto));

        // Quando chamar listAllByFilter ele retorna o mock paginaMock
        when(permissaoService.listAllByFilter(any(), any())).thenReturn(paginaMock);

        // chama o endpoint GET /v1/permissao
        mockMvc.perform(get("/v1/permissao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cargo.nome").value("Cargo"))
                .andExpect(jsonPath("$.content[0].operacao.nome").value("Operacao"));

        // verifica se o service foi chamado
        verify(permissaoService).listAllByFilter(any(), any());
    }

    @Test
    @DisplayName("Should return permissao with status 200 when id exists")
    void findCase1() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Service
        CargoEntity cargo = CargoEntity.builder().id(UUID.randomUUID()).nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().id(UUID.randomUUID()).nome("Operacao").codigo("OP1").build();
        PermissaoResponseDTO dto = PermissaoResponseDTO.builder().id(id).cargo(cargo).operacao(operacao).build();

        // Quando chamar find ele retorna o mock dto
        when(permissaoService.find(any(), eq(id))).thenReturn(dto);

        // chama o endpoint GET /v1/permissao/{id}
        mockMvc.perform(get("/v1/permissao/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.cargo.nome").value("Cargo"));

        // verifica se o service foi chamado com o id correto
        verify(permissaoService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when id does not exist")
    void findCase2() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular que o service lanca excecao pois a permissao nao existe
        when(permissaoService.find(any(), eq(id))).thenThrow(new NaoEncontradoException("Permissão não encontrada"));

        // chama o endpoint GET /v1/permissao/{id} e verifica se retorna 404
        mockMvc.perform(get("/v1/permissao/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(permissaoService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should create permissao with status 201")
    void createCase1() throws Exception {
        // dados para criacao
        UUID idCargo = UUID.randomUUID();
        UUID idOperacao = UUID.randomUUID();
        PermissaoCreateDTO permissao = PermissaoCreateDTO.builder().idCargo(idCargo).idOperacao(idOperacao).build();

        // Mock para simular resposta do Service
        UUID id = UUID.randomUUID();
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().id(idOperacao).nome("Operacao").codigo("OP1").build();
        PermissaoResponseDTO dtoSalvo = PermissaoResponseDTO.builder().id(id).cargo(cargo).operacao(operacao).build();

        // Quando chamar create ele retorna o mock dtoSalvo
        when(permissaoService.create(any(PermissaoCreateDTO.class))).thenReturn(dtoSalvo);

        // chama o endpoint POST /v1/permissao
        mockMvc.perform(post("/v1/permissao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissao)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.cargo.nome").value("Cargo"));

        // verifica se o service foi chamado com os dados corretos
        verify(permissaoService).create(argThat(p -> p.getIdCargo().equals(idCargo) && p.getIdOperacao().equals(idOperacao)));
    }

    @Test
    @DisplayName("Should return status 404 when creating permissao with a cargo that does not exist")
    void createCase2() throws Exception {
        // dados para criacao com cargo inexistente
        PermissaoCreateDTO permissao = PermissaoCreateDTO.builder().idCargo(UUID.randomUUID()).idOperacao(UUID.randomUUID()).build();

        // Mock para simular que o service lanca excecao pois o cargo nao existe
        when(permissaoService.create(any(PermissaoCreateDTO.class))).thenThrow(new NaoEncontradoException("Cargo inválido"));

        // chama o endpoint POST /v1/permissao e verifica se retorna 404
        mockMvc.perform(post("/v1/permissao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissao)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com os dados corretos
        verify(permissaoService).create(any(PermissaoCreateDTO.class));
    }

    @Test
    @DisplayName("Should update permissao with status 200 when id exists")
    void updateCase1() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UUID idCargo = UUID.randomUUID();
        UUID idOperacao = UUID.randomUUID();
        PermissaoCreateDTO permissao = PermissaoCreateDTO.builder().idCargo(idCargo).idOperacao(idOperacao).build();

        // Mock para simular resposta do Service
        CargoEntity cargo = CargoEntity.builder().id(idCargo).nome("Cargo").build();
        OperacaoEntity operacao = OperacaoEntity.builder().id(idOperacao).nome("Operacao").codigo("OP1").build();
        PermissaoResponseDTO dtoAtualizado = PermissaoResponseDTO.builder().id(id).cargo(cargo).operacao(operacao).build();
        when(permissaoService.update(any(PermissaoCreateDTO.class), eq(id))).thenReturn(dtoAtualizado);

        // chama o endpoint PUT /v1/permissao/{id}
        mockMvc.perform(put("/v1/permissao/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.cargo.nome").value("Cargo"));

        // verifica se o service foi chamado com o id e os dados corretos
        verify(permissaoService).update(argThat(p -> p.getIdCargo().equals(idCargo) && p.getIdOperacao().equals(idOperacao)), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when updating a permissao that does not exist")
    void updateCase2() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        PermissaoCreateDTO permissao = PermissaoCreateDTO.builder().idCargo(UUID.randomUUID()).idOperacao(UUID.randomUUID()).build();

        // Mock para simular que o service lanca excecao pois a permissao nao existe
        when(permissaoService.update(any(PermissaoCreateDTO.class), eq(id))).thenThrow(new NaoEncontradoException("Permissão não encontrada"));

        // chama o endpoint PUT /v1/permissao/{id} e verifica se retorna 404
        mockMvc.perform(put("/v1/permissao/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissao)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(permissaoService).update(any(PermissaoCreateDTO.class), eq(id));
    }

    @Test
    @DisplayName("Should delete permissao with status 204")
    void deleteCase1() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service exclui sem lancar excecao
        doNothing().when(permissaoService).delete(id);

        // chama o endpoint DELETE /v1/permissao/{id}
        mockMvc.perform(delete("/v1/permissao/{id}", id))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id correto
        verify(permissaoService).delete(id);
    }
}
