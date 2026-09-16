package com.nowbox.nowbox_api.security;

import com.nowbox.nowbox_api.modules.atribuicao.repository.IAtribuicaoRepository;
import com.nowbox.nowbox_api.modules.permissao.repository.IPermissaoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final IAtribuicaoRepository atribuicaoRepository;
    private final IPermissaoRepository permissaoRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Le o header Authorization da requisicao "Authorization: Bearer <token aqui>"
        String header = request.getHeader("Authorization");

        // So tenta autenticar se o header existir e estiver no formato esperado "Bearer <token>".
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            // Remove o prefixo "Bearer " os 7 primeiros caracteres e valida a assinatura e a expiracao do JWT,
            // extraindo o id do usuario do token. Retorna null se o token for invalido ou tiver expirado
            UUID idUsuario = jwtService.validarToken(header.substring(7));

            if (idUsuario != null) {
                // busca no banco as permissoes e as unidades atribuidas ao usuario autenticado
                Set<String> permissoes = permissaoRepository.findCodigosByUsuario(idUsuario);
                Set<UUID> unidades = atribuicaoRepository.findUnidadesIdsByUsuario(idUsuario);

                // Monta o usuario autenticado com os dados de dominio da aplicacao
                UsuarioAutenticado principal = new UsuarioAutenticado(idUsuario, permissoes, unidades);
                // Converte cada permissao em GrantedAuthority do Spring Security para ser lido depois pelo hasAuthority no @PreAuthorize
                List<GrantedAuthority> authorities = permissoes.stream()
                        .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                        .toList();

                // Registra a Authentication no SecurityContextHolder da requisicao atual, autenticando a requisicao
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, authorities));
            }
        }

        // Continua a execucao da cadeia de filtros
        filterChain.doFilter(request, response);
    }

}
