package com.nowbox.nowbox_api.modules.menu.service;

import com.nowbox.nowbox_api.modules.menu.dto.MenuSessaoResponseDTO;
import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import com.nowbox.nowbox_api.modules.modulo.repository.IModuloRepository;
import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private IModuloRepository moduloRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    @DisplayName("Should group the modulos of the usuario by sessao keeping the repository order")
    void listByUsuarioCase1() {
        // id do usuario autenticado
        UUID idUsuario = UUID.randomUUID();

        // Mock para simular resposta do Repository com duas sessoes
        SessaoEntity sessaoA = SessaoEntity.builder().id(UUID.randomUUID()).nome("Sessao A").rota("/sessaoA").build();
        SessaoEntity sessaoB = SessaoEntity.builder().id(UUID.randomUUID()).nome("Sessao B").rota("/sessaoB").build();
        ModuloEntity modulo1 = ModuloEntity.builder().id(UUID.randomUUID()).nome("Modulo A1").rota("/moduloA1").sessao(sessaoA).build();
        ModuloEntity modulo2 = ModuloEntity.builder().id(UUID.randomUUID()).nome("Modulo A2").rota("/moduloA2").sessao(sessaoA).build();
        ModuloEntity modulo3 = ModuloEntity.builder().id(UUID.randomUUID()).nome("Modulo B1").rota("/moduloB1").sessao(sessaoB).build();

        // Quando chamar findAllByUsuario ele retorna os modulos permitidos
        when(moduloRepository.findAllByUsuario(idUsuario)).thenReturn(List.of(modulo1, modulo2, modulo3));

        // chama a funcao listByUsuario
        List<MenuSessaoResponseDTO> result = menuService.listByUsuario(idUsuario);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getId()).isEqualTo(sessaoA.getId());
        assertThat(result.get(0).getNome()).isEqualTo("Sessao A");
        assertThat(result.get(0).getRota()).isEqualTo("/sessaoA");
        assertThat(result.get(0).getModulos()).hasSize(2);
        assertThat(result.get(0).getModulos().get(0).getId()).isEqualTo(modulo1.getId());
        assertThat(result.get(0).getModulos().get(0).getNome()).isEqualTo("Modulo A1");
        assertThat(result.get(0).getModulos().get(0).getRota()).isEqualTo("/moduloA1");
        assertThat(result.get(0).getModulos().get(1).getNome()).isEqualTo("Modulo A2");

        assertThat(result.get(1).getNome()).isEqualTo("Sessao B");
        assertThat(result.get(1).getModulos()).hasSize(1);
        assertThat(result.get(1).getModulos().getFirst().getNome()).isEqualTo("Modulo B1");

        // verifica se ao chamar a findAllByUsuario ele passou o mesmo id
        verify(moduloRepository).findAllByUsuario(idUsuario);
    }

    @Test
    @DisplayName("Should return empty list when the usuario has no permitted modulo")
    void listByUsuarioCase2() {
        // id do usuario autenticado
        UUID idUsuario = UUID.randomUUID();

        // Quando chamar findAllByUsuario ele retorna vazio
        when(moduloRepository.findAllByUsuario(idUsuario)).thenReturn(List.of());

        // chama a funcao listByUsuario
        List<MenuSessaoResponseDTO> result = menuService.listByUsuario(idUsuario);

        assertThat(result).isEmpty();

        // verifica se ao chamar a findAllByUsuario ele passou o mesmo id
        verify(moduloRepository).findAllByUsuario(idUsuario);
    }
}
