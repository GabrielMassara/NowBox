package com.nowbox.nowbox_api.security;

import com.nowbox.nowbox_api.modules.atribuicao.repository.IAtribuicaoRepository;
import com.nowbox.nowbox_api.modules.permissao.repository.IPermissaoRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private IAtribuicaoRepository atribuicaoRepository;

    @Mock
    private IPermissaoRepository permissaoRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        // limpa o contexto de seguranca entre os testes para nao vazar autenticacao de um teste para outro
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate the request when the token is valid")
    void doFilterInternalCase1() throws Exception {
        UUID idUsuario = UUID.randomUUID();
        UUID idUnidade = UUID.randomUUID();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Mock para simular token valido e as permissoes/unidades do usuario
        when(jwtService.validarToken("token-valido")).thenReturn(idUsuario);
        when(permissaoRepository.findCodigosByUsuario(idUsuario)).thenReturn(Set.of("MOD_TESTE_OPE_CONSULTAR"));
        when(atribuicaoRepository.findUnidadesIdsByUsuario(idUsuario)).thenReturn(Set.of(idUnidade));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // verifica se a Authentication foi montada corretamente no SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(UsuarioAutenticado.class);

        UsuarioAutenticado principal = (UsuarioAutenticado) authentication.getPrincipal();
        assertThat(principal.getId()).isEqualTo(idUsuario);
        assertThat(principal.getUnidades()).containsExactly(idUnidade);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("MOD_TESTE_OPE_CONSULTAR");

        // verifica se a cadeia de filtros continuou normalmente
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate and continue the chain when there is no Authorization header")
    void doFilterInternalCase2() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        // nao deve nem tentar validar token, ja que nao ha header
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Should not authenticate when header does not start with Bearer")
    void doFilterInternalCase3() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic algumacoisa");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Should not authenticate when token is invalid or expired")
    void doFilterInternalCase4() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Mock para simular token invalido/expirado
        when(jwtService.validarToken("token-invalido")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        // nao deve consultar permissoes/unidades se o token nao foi validado
        verifyNoInteractions(permissaoRepository, atribuicaoRepository);
    }

}
