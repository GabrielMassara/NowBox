package com.nowbox.nowbox_api.modules.operacao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import com.nowbox.nowbox_api.modules.operacao.dto.OperacaoCreateDTO;
import com.nowbox.nowbox_api.modules.operacao.dto.OperacaoResponseDTO;
import com.nowbox.nowbox_api.modules.operacao.service.OperacaoService;
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

@WebMvcTest(OperacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class OperacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OperacaoService operacaoService;

    @Test
    @DisplayName("Should list operacoes with status 200")
    void listAll() throws Exception {
        // Mock para simular resposta do Service
        ModuloEntity modulo = ModuloEntity.builder().id(UUID.randomUUID()).nome("Modulo").rota("/modulo").build();
        OperacaoResponseDTO dto = OperacaoResponseDTO.builder().id(UUID.randomUUID()).nome("Operacao Test").codigo("OP1").modulo(modulo).build();
        Page<OperacaoResponseDTO> paginaMock = new PageImpl<>(List.of(dto));

        // Quando chamar listAllByFilter ele retorna o mock paginaMock
        when(operacaoService.listAllByFilter(any(), any())).thenReturn(paginaMock);

        // chama o endpoint GET /v1/operacao
        mockMvc.perform(get("/v1/operacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Operacao Test"))
                .andExpect(jsonPath("$.content[0].codigo").value("OP1"));

        // verifica se o service foi chamado
        verify(operacaoService).listAllByFilter(any(), any());
    }

    @Test
    @DisplayName("Should return operacao with status 200 when id exists")
    void findCase1() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Service
        ModuloEntity modulo = ModuloEntity.builder().id(UUID.randomUUID()).nome("Modulo").rota("/modulo").build();
        OperacaoResponseDTO dto = OperacaoResponseDTO.builder().id(id).nome("Operacao Test").codigo("OP1").modulo(modulo).build();

        // Quando chamar find ele retorna o mock dto
        when(operacaoService.find(any(), eq(id))).thenReturn(dto);

        // chama o endpoint GET /v1/operacao/{id}
        mockMvc.perform(get("/v1/operacao/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Operacao Test"));

        // verifica se o service foi chamado com o id correto
        verify(operacaoService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when id does not exist")
    void findCase2() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular que o service lanca excecao pois a operacao nao existe
        when(operacaoService.find(any(), eq(id))).thenThrow(new NaoEncontradoException("Operação não encontrada"));

        // chama o endpoint GET /v1/operacao/{id} e verifica se retorna 404
        mockMvc.perform(get("/v1/operacao/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(operacaoService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should create operacao with status 201")
    void createCase1() throws Exception {
        // dados para criacao
        UUID idModulo = UUID.randomUUID();
        OperacaoCreateDTO operacao = OperacaoCreateDTO.builder().idModulo(idModulo).nome("Operacao Test").codigo("OP1").build();

        // Mock para simular resposta do Service
        UUID id = UUID.randomUUID();
        ModuloEntity modulo = ModuloEntity.builder().id(idModulo).nome("Modulo").rota("/modulo").build();
        OperacaoResponseDTO dtoSalvo = OperacaoResponseDTO.builder().id(id).nome("Operacao Test").codigo("OP1").modulo(modulo).build();

        // Quando chamar create ele retorna o mock dtoSalvo
        when(operacaoService.create(any(OperacaoCreateDTO.class))).thenReturn(dtoSalvo);

        // chama o endpoint POST /v1/operacao
        mockMvc.perform(post("/v1/operacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operacao)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Operacao Test"));

        // verifica se o service foi chamado com os dados corretos
        verify(operacaoService).create(argThat(o -> o.getIdModulo().equals(idModulo) && o.getNome().equals("Operacao Test") && o.getCodigo().equals("OP1")));
    }

    @Test
    @DisplayName("Should return status 404 when creating operacao with a modulo that does not exist")
    void createCase2() throws Exception {
        // dados para criacao com modulo inexistente
        OperacaoCreateDTO operacao = OperacaoCreateDTO.builder().idModulo(UUID.randomUUID()).nome("Operacao Test").codigo("OP1").build();

        // Mock para simular que o service lanca excecao pois o modulo nao existe
        when(operacaoService.create(any(OperacaoCreateDTO.class))).thenThrow(new NaoEncontradoException("Módulo inválido"));

        // chama o endpoint POST /v1/operacao e verifica se retorna 404
        mockMvc.perform(post("/v1/operacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operacao)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com os dados corretos
        verify(operacaoService).create(any(OperacaoCreateDTO.class));
    }

    @Test
    @DisplayName("Should update operacao with status 200 when id exists")
    void updateCase1() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UUID idModulo = UUID.randomUUID();
        OperacaoCreateDTO operacao = OperacaoCreateDTO.builder().idModulo(idModulo).nome("Operacao Test").codigo("OP1").build();

        // Mock para simular resposta do Service
        ModuloEntity modulo = ModuloEntity.builder().id(idModulo).nome("Modulo").rota("/modulo").build();
        OperacaoResponseDTO dtoAtualizado = OperacaoResponseDTO.builder().id(id).nome("Operacao Test").codigo("OP1").modulo(modulo).build();
        when(operacaoService.update(any(OperacaoCreateDTO.class), eq(id))).thenReturn(dtoAtualizado);

        // chama o endpoint PUT /v1/operacao/{id}
        mockMvc.perform(put("/v1/operacao/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Operacao Test"));

        // verifica se o service foi chamado com o id e os dados corretos
        verify(operacaoService).update(argThat(o -> o.getNome().equals("Operacao Test") && o.getCodigo().equals("OP1")), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when updating a operacao that does not exist")
    void updateCase2() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        OperacaoCreateDTO operacao = OperacaoCreateDTO.builder().idModulo(UUID.randomUUID()).nome("Operacao Test").codigo("OP1").build();

        // Mock para simular que o service lanca excecao pois a operacao nao existe
        when(operacaoService.update(any(OperacaoCreateDTO.class), eq(id))).thenThrow(new NaoEncontradoException("Operação não encontrada"));

        // chama o endpoint PUT /v1/operacao/{id} e verifica se retorna 404
        mockMvc.perform(put("/v1/operacao/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operacao)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(operacaoService).update(any(OperacaoCreateDTO.class), eq(id));
    }

    @Test
    @DisplayName("Should delete operacao with status 204")
    void deleteCase1() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service exclui sem lancar excecao
        doNothing().when(operacaoService).delete(id);

        // chama o endpoint DELETE /v1/operacao/{id}
        mockMvc.perform(delete("/v1/operacao/{id}", id))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id correto
        verify(operacaoService).delete(id);
    }
}
