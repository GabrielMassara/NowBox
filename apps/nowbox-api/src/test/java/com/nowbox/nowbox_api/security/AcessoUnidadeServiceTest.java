package com.nowbox.nowbox_api.security;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcessoUnidadeServiceTest {

    @Mock
    private IBoxRepository boxRepository;

    @Mock
    private IAluguelRepository aluguelRepository;

    @InjectMocks
    private AcessoUnidadeService acessoUnidadeService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(UUID... unidades) {
        UsuarioAutenticado principal = new UsuarioAutenticado(UUID.randomUUID(), Set.of(), Set.of(unidades));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of()));
    }

    @Test
    @DisplayName("Should grant access when the authenticated user has the unidade")
    void temAcessoCase1() {
        UUID idUnidade = UUID.randomUUID();
        autenticarComo(idUnidade);

        assertThat(acessoUnidadeService.temAcesso(idUnidade)).isTrue();
    }

    @Test
    @DisplayName("Should deny access when the authenticated user does not have the unidade")
    void temAcessoCase2() {
        autenticarComo(UUID.randomUUID());

        assertThat(acessoUnidadeService.temAcesso(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Should deny access when idUnidade is null")
    void temAcessoCase3() {
        autenticarComo(UUID.randomUUID());

        assertThat(acessoUnidadeService.temAcesso(null)).isFalse();
    }

    @Test
    @DisplayName("Should deny access when principal is not a UsuarioAutenticado")
    void temAcessoCase4() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonimo", null, java.util.List.of()));

        assertThat(acessoUnidadeService.temAcesso(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Should grant access to a box when the user has the box's unidade")
    void temAcessoBoxCase1() {
        UUID idUnidade = UUID.randomUUID();
        UUID idBox = UUID.randomUUID();
        autenticarComo(idUnidade);

        // Mock para simular que o box existe e pertence a unidade que o usuario tem acesso
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).build();
        BoxEntity box = BoxEntity.builder().id(idBox).unidade(unidade).build();
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));

        assertThat(acessoUnidadeService.temAcessoBox(idBox)).isTrue();
    }

    @Test
    @DisplayName("Should deny access to a box when the user does not have the box's unidade")
    void temAcessoBoxCase2() {
        UUID idBox = UUID.randomUUID();
        autenticarComo(UUID.randomUUID());

        // Mock para simular que o box existe e pertence a uma unidade diferente da que o usuario tem acesso
        UnidadeEntity outraUnidade = UnidadeEntity.builder().id(UUID.randomUUID()).build();
        BoxEntity box = BoxEntity.builder().id(idBox).unidade(outraUnidade).build();
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.of(box));

        assertThat(acessoUnidadeService.temAcessoBox(idBox)).isFalse();
    }

    @Test
    @DisplayName("Should grant access when box does not exist, so the service can throw 404 later")
    void temAcessoBoxCase3() {
        UUID idBox = UUID.randomUUID();

        // Mock para simular que o box nao existe: acesso liberado para o 404 ser lancado depois
        when(boxRepository.findByIdAndDeletedAtIsNull(idBox)).thenReturn(Optional.empty());

        assertThat(acessoUnidadeService.temAcessoBox(idBox)).isTrue();
    }

    @Test
    @DisplayName("Should deny access to a box when idBox is null")
    void temAcessoBoxCase4() {
        assertThat(acessoUnidadeService.temAcessoBox(null)).isFalse();
    }

    @Test
    @DisplayName("Should grant access to an aluguel when the user has the aluguel box's unidade")
    void temAcessoAluguelCase1() {
        UUID idUnidade = UUID.randomUUID();
        UUID idAluguel = UUID.randomUUID();
        autenticarComo(idUnidade);

        // Mock para simular que o aluguel existe e o box dele pertence a unidade que o usuario tem acesso
        UnidadeEntity unidade = UnidadeEntity.builder().id(idUnidade).build();
        BoxEntity box = BoxEntity.builder().id(UUID.randomUUID()).unidade(unidade).build();
        AluguelEntity aluguel = AluguelEntity.builder().id(idAluguel).box(box).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.of(aluguel));

        assertThat(acessoUnidadeService.temAcessoAluguel(idAluguel)).isTrue();
    }

    @Test
    @DisplayName("Should deny access to an aluguel when the user does not have the aluguel box's unidade")
    void temAcessoAluguelCase2() {
        UUID idAluguel = UUID.randomUUID();
        autenticarComo(UUID.randomUUID());

        // Mock para simular que o aluguel existe e o box dele pertence a uma unidade diferente
        UnidadeEntity outraUnidade = UnidadeEntity.builder().id(UUID.randomUUID()).build();
        BoxEntity box = BoxEntity.builder().id(UUID.randomUUID()).unidade(outraUnidade).build();
        AluguelEntity aluguel = AluguelEntity.builder().id(idAluguel).box(box).build();
        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.of(aluguel));

        assertThat(acessoUnidadeService.temAcessoAluguel(idAluguel)).isFalse();
    }

    @Test
    @DisplayName("Should grant access when aluguel does not exist, so the service can throw 404 later")
    void temAcessoAluguelCase3() {
        UUID idAluguel = UUID.randomUUID();

        when(aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)).thenReturn(Optional.empty());

        assertThat(acessoUnidadeService.temAcessoAluguel(idAluguel)).isTrue();
    }

    @Test
    @DisplayName("Should deny access to an aluguel when idAluguel is null")
    void temAcessoAluguelCase4() {
        assertThat(acessoUnidadeService.temAcessoAluguel(null)).isFalse();
    }

}
