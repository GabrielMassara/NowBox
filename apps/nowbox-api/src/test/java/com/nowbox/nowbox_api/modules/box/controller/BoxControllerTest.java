package com.nowbox.nowbox_api.modules.box.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.box.dto.BoxCreateDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxResponseDTO;
import com.nowbox.nowbox_api.modules.box.service.BoxService;
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

@WebMvcTest(BoxController.class)
@AutoConfigureMockMvc(addFilters = false)
class BoxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BoxService boxService;

    @Test
    @DisplayName("Should list boxes with status 200")
    void listAll() throws Exception {
        // Mock para simular resposta do Service
        UnidadeEntity unidade = UnidadeEntity.builder().id(UUID.randomUUID()).nome("Unidade Test").build();
        BoxResponseDTO dto = BoxResponseDTO.builder().id(UUID.randomUUID()).numero("101").unidade(unidade).build();
        Page<BoxResponseDTO> paginaMock = new PageImpl<>(List.of(dto));

        // Quando chamar listAllByFilter ele retorna o mock paginaMock
        when(boxService.listAllByFilter(any(), any())).thenReturn(paginaMock);

        // chama o endpoint GET /v1/box
        mockMvc.perform(get("/v1/box"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numero").value("101"));

        // verifica se o service foi chamado
        verify(boxService).listAllByFilter(any(), any());
    }

    @Test
    @DisplayName("Should return box with status 200 when id exists")
    void findCase1() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular resposta do Service
        UnidadeEntity unidade = UnidadeEntity.builder().id(UUID.randomUUID()).nome("Unidade Test").build();
        BoxResponseDTO dto = BoxResponseDTO.builder().id(id).numero("101").unidade(unidade).build();

        // Quando chamar find ele retorna o mock dto
        when(boxService.find(any(), eq(id))).thenReturn(dto);

        // chama o endpoint GET /v1/box/{id}
        mockMvc.perform(get("/v1/box/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.numero").value("101"));

        // verifica se o service foi chamado com o id correto
        verify(boxService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when id does not exist")
    void findCase2() throws Exception {
        // id utilizado na busca
        UUID id = UUID.randomUUID();

        // Mock para simular que o service lanca excecao pois o box nao existe
        when(boxService.find(any(), eq(id))).thenThrow(new NaoEncontradoException("Box não encontrado"));

        // chama o endpoint GET /v1/box/{id} e verifica se retorna 404
        mockMvc.perform(get("/v1/box/{id}", id))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id correto
        verify(boxService).find(any(), eq(id));
    }

    @Test
    @DisplayName("Should create box with status 201")
    void createCase1() throws Exception {
        // dados para criacao
        UUID idUnidade = UUID.randomUUID();
        BoxCreateDTO box = BoxCreateDTO.builder().idUnidade(idUnidade).numero("101").preco(BigDecimal.valueOf(150.00)).build();

        // Mock para simular resposta do Service
        UUID id = UUID.randomUUID();
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();
        BoxResponseDTO dtoSalvo = BoxResponseDTO.builder().id(id).numero("101").unidade(unidade).preco(BigDecimal.valueOf(150.00)).build();

        // Quando chamar create ele retorna o mock dtoSalvo
        when(boxService.create(any(BoxCreateDTO.class))).thenReturn(dtoSalvo);

        // chama o endpoint POST /v1/box
        mockMvc.perform(post("/v1/box")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(box)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.numero").value("101"));

        // verifica se o service foi chamado com os dados corretos
        verify(boxService).create(argThat(b -> b.getIdUnidade().equals(idUnidade) && b.getNumero().equals("101")));
    }

    @Test
    @DisplayName("Should return status 404 when creating box with a unidade that does not exist")
    void createCase2() throws Exception {
        // dados para criacao com unidade inexistente
        BoxCreateDTO box = BoxCreateDTO.builder().idUnidade(UUID.randomUUID()).numero("101").build();

        // Mock para simular que o service lanca excecao pois a unidade nao existe
        when(boxService.create(any(BoxCreateDTO.class))).thenThrow(new NaoEncontradoException("Unidade inválida"));

        // chama o endpoint POST /v1/box e verifica se retorna 404
        mockMvc.perform(post("/v1/box")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(box)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com os dados corretos
        verify(boxService).create(any(BoxCreateDTO.class));
    }

    @Test
    @DisplayName("Should update box with status 200 when id exists")
    void updateCase1() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        UUID idUnidade = UUID.randomUUID();
        BoxCreateDTO box = BoxCreateDTO.builder().idUnidade(idUnidade).numero("101").build();

        // Mock para simular resposta do Service
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).nome("Unidade Test").build();
        BoxResponseDTO dtoAtualizado = BoxResponseDTO.builder().id(id).numero("101").unidade(unidade).build();
        when(boxService.update(any(BoxCreateDTO.class), eq(id))).thenReturn(dtoAtualizado);

        // chama o endpoint PUT /v1/box/{id}
        mockMvc.perform(put("/v1/box/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(box)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.numero").value("101"));

        // verifica se o service foi chamado com o id e os dados corretos
        verify(boxService).update(argThat(b -> b.getNumero().equals("101")), eq(id));
    }

    @Test
    @DisplayName("Should return status 404 when updating a box that does not exist")
    void updateCase2() throws Exception {
        // id e dados para atualizacao
        UUID id = UUID.randomUUID();
        BoxCreateDTO box = BoxCreateDTO.builder().idUnidade(UUID.randomUUID()).numero("101").build();

        // Mock para simular que o service lanca excecao pois o box nao existe
        when(boxService.update(any(BoxCreateDTO.class), eq(id))).thenThrow(new NaoEncontradoException("Box não encontrado"));

        // chama o endpoint PUT /v1/box/{id} e verifica se retorna 404
        mockMvc.perform(put("/v1/box/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(box)))
                .andExpect(status().isNotFound());

        // verifica se o service foi chamado com o id e os dados corretos
        verify(boxService).update(any(BoxCreateDTO.class), eq(id));
    }

    @Test
    @DisplayName("Should delete box with status 204")
    void deleteCase1() throws Exception {
        // id utilizado para exclusao
        UUID id = UUID.randomUUID();

        // Mock para simular que o service exclui sem lancar excecao
        doNothing().when(boxService).delete(id);

        // chama o endpoint DELETE /v1/box/{id}
        mockMvc.perform(delete("/v1/box/{id}", id))
                .andExpect(status().isNoContent());

        // verifica se o service foi chamado com o id correto
        verify(boxService).delete(id);
    }
}
