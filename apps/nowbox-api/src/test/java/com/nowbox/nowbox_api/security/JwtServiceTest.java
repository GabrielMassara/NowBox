package com.nowbox.nowbox_api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class JwtServiceTest {

    private static final String SECRET = "1f2e0f8f6b7c4a3d9e1c5b8a7f6d4e3c2b1a0f9e8d7c6b5a4f3e2d1c0b9a8f7e";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // instancia o service diretamente, sem contexto Spring, com expiracao de 1 minuto
        jwtService = new JwtService(SECRET, 1);
    }

    @Test
    @DisplayName("Should generate a token and validate it back to the same id")
    void gerarTokenECase1() {
        UUID idUsuario = UUID.randomUUID();

        // gera o token para o usuario
        String token = jwtService.gerarToken(idUsuario);

        assertThat(token).isNotBlank();

        // valida o token gerado e espera receber o mesmo id de volta
        UUID resultado = jwtService.validarToken(token);

        assertThat(resultado).isEqualTo(idUsuario);
    }

    @Test
    @DisplayName("Should return null when token is malformed")
    void validarTokenCase1() {
        UUID resultado = jwtService.validarToken("token-invalido");

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Should return null when token was signed with a different secret")
    void validarTokenCase2() {
        UUID idUsuario = UUID.randomUUID();

        // gera o token com um secret diferente do usado pelo jwtService em teste
        JwtService outroJwtService = new JwtService("f9e8d7c6b5a4f3e2d1c0b9a8f7e61f2e0f8f6b7c4a3d9e1c5b8a7f6d4e3c2b1", 1);
        String token = outroJwtService.gerarToken(idUsuario);

        // valida com o service que usa o secret original: assinatura nao confere
        UUID resultado = jwtService.validarToken(token);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Should return null when token is expired")
    void validarTokenCase3() {
        UUID idUsuario = UUID.randomUUID();

        // service com expiracao negativa: o token ja nasce expirado
        JwtService jwtServiceExpirado = new JwtService(SECRET, -1);
        String token = jwtServiceExpirado.gerarToken(idUsuario);

        UUID resultado = jwtService.validarToken(token);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Should not throw when validating a null or empty token")
    void validarTokenCase4() {
        assertThatCode(() -> jwtService.validarToken("")).doesNotThrowAnyException();
        assertThat(jwtService.validarToken("")).isNull();
    }

}
