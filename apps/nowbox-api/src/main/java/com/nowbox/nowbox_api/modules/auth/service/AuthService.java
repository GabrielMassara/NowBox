package com.nowbox.nowbox_api.modules.auth.service;

import com.nowbox.nowbox_api.common.exception.CredenciaisInvalidasException;
import com.nowbox.nowbox_api.modules.auth.dto.LoginRequestDTO;
import com.nowbox.nowbox_api.modules.auth.dto.LoginResponseDTO;
import com.nowbox.nowbox_api.modules.usuario.entity.UsuarioEntity;
import com.nowbox.nowbox_api.modules.usuario.repository.IUsuarioRepository;
import com.nowbox.nowbox_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO login) {
        UsuarioEntity usuario = usuarioRepository.findByEmailAndDeletedAtIsNull(login.getEmail())
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(login.getSenha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException("E-mail ou senha inválidos");
        }

        return LoginResponseDTO.builder().token(jwtService.gerarToken(usuario.getId())).build();
    }

}
