package com.nowbox.nowbox_api.modules.auth.service;

import com.nowbox.nowbox_api.common.exception.CredenciaisInvalidasException;
import com.nowbox.nowbox_api.modules.auth.dto.LoginRequestDTO;
import com.nowbox.nowbox_api.modules.auth.dto.LoginResponseDTO;
import com.nowbox.nowbox_api.modules.usuario.entity.UsuarioEntity;
import com.nowbox.nowbox_api.modules.usuario.repository.IUsuarioRepository;
import com.nowbox.nowbox_api.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should return a token when email and senha are valid")
    void loginCase1() {
        UUID id = UUID.randomUUID();
        LoginRequestDTO login = LoginRequestDTO.builder().email("usuario@test.com").senha("senha123").build();

        // Mock para simular que o usuario existe e a senha confere
        UsuarioEntity usuario = UsuarioEntity.builder().id(id).email("usuario@test.com").senha("senhaHasheada").build();
        when(usuarioRepository.findByEmailAndDeletedAtIsNull("usuario@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "senhaHasheada")).thenReturn(true);
        when(jwtService.gerarToken(id)).thenReturn("token-gerado");

        LoginResponseDTO result = authService.login(login);

        assertThat(result.getToken()).isEqualTo("token-gerado");

        // verifica se o token foi gerado para o id correto
        verify(jwtService).gerarToken(id);
    }

    @Test
    @DisplayName("Should throw exception when email does not exist")
    void loginCase2() {
        LoginRequestDTO login = LoginRequestDTO.builder().email("naoexiste@test.com").senha("senha123").build();

        // Mock para simular que o email nao existe (ou esta deletado)
        when(usuarioRepository.findByEmailAndDeletedAtIsNull("naoexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(CredenciaisInvalidasException.class, () -> authService.login(login));

        // nao deve chegar a gerar token
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    @DisplayName("Should throw exception when senha does not match")
    void loginCase3() {
        LoginRequestDTO login = LoginRequestDTO.builder().email("usuario@test.com").senha("senhaErrada").build();

        // Mock para simular que o usuario existe mas a senha nao confere
        UsuarioEntity usuario = UsuarioEntity.builder().id(UUID.randomUUID()).email("usuario@test.com").senha("senhaHasheada").build();
        when(usuarioRepository.findByEmailAndDeletedAtIsNull("usuario@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", "senhaHasheada")).thenReturn(false);

        assertThrows(CredenciaisInvalidasException.class, () -> authService.login(login));

        // nao deve chegar a gerar token
        verify(jwtService, never()).gerarToken(any());
    }

}
