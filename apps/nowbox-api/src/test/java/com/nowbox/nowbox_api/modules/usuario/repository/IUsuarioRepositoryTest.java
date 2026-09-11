package com.nowbox.nowbox_api.modules.usuario.repository;

import com.nowbox.nowbox_api.modules.usuario.entity.UsuarioEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class IUsuarioRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    IUsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Return usuario with name filter")
    void findAllByFilterCase1() {
        List<UsuarioEntity> usuarios = this.createScenario();

        Page<UsuarioEntity> result = usuarioRepository.findAllByFilter(usuarios.get(0).getNome(), null, null, null);

        assertThat(result.getContent().getFirst().getNome()).isEqualTo(usuarios.get(0).getNome());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return usuario with email filter")
    void findAllByFilterCase2() {
        List<UsuarioEntity> usuarios = this.createScenario();

        Page<UsuarioEntity> result = usuarioRepository.findAllByFilter(null, usuarios.get(1).getEmail(), null, null);

        assertThat(result.getContent().getFirst().getEmail()).isEqualTo(usuarios.get(1).getEmail());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return usuario with cpf filter")
    void findAllByFilterCase3() {
        List<UsuarioEntity> usuarios = this.createScenario();

        Page<UsuarioEntity> result = usuarioRepository.findAllByFilter(null, null, usuarios.get(2).getCpf(), null);

        assertThat(result.getContent().getFirst().getCpf()).isEqualTo(usuarios.get(2).getCpf());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Return all elements of usuario")
    void findAllByFilterCase4() {
        this.createScenario();

        Page<UsuarioEntity> result = usuarioRepository.findAllByFilter(null, null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("No elements created: Should not return elements of usuario")
    void findAllByFilterCase5() {
        Page<UsuarioEntity> result = usuarioRepository.findAllByFilter(null, null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("No filter match: Should not return elements of usuario")
    void findAllByFilterCase6() {
        this.createScenario();

        Page<UsuarioEntity> result = usuarioRepository.findAllByFilter("Usuario Inexistente", null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should not return soft deleted usuario")
    void findAllByFilterCase7() {
        List<UsuarioEntity> usuarios = this.createScenario();
        UsuarioEntity usuario1 = usuarios.get(0);
        usuario1.setDeletedAt(LocalDateTime.now());
        this.em.persist(usuario1);

        Page<UsuarioEntity> result = usuarioRepository.findAllByFilter(usuario1.getNome(), null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should return usuario when id exists and is not deleted")
    void findByIdAndDeletedAtIsNullCase1() {
        List<UsuarioEntity> usuarios = this.createScenario();
        UsuarioEntity usuario1 = usuarios.get(0);

        Optional<UsuarioEntity> result = usuarioRepository.findByIdAndDeletedAtIsNull(usuario1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getNome()).isEqualTo(usuario1.getNome());
    }

    @Test
    @DisplayName("Should not return usuario when id is soft deleted")
    void findByIdAndDeletedAtIsNullCase2() {
        List<UsuarioEntity> usuarios = this.createScenario();
        UsuarioEntity usuario1 = usuarios.get(0);
        usuario1.setDeletedAt(LocalDateTime.now());
        this.em.persist(usuario1);

        Optional<UsuarioEntity> result = usuarioRepository.findByIdAndDeletedAtIsNull(usuario1.getId());

        assertThat(result).isEmpty();
    }

    private List<UsuarioEntity> createScenario() {
        UsuarioEntity usuario1 = UsuarioEntity.builder()
                .nome("Usuario Test 1").email("usuario1@test.com").cpf("11111111111").sexo("M").senha("senha1").build();
        UsuarioEntity usuario2 = UsuarioEntity.builder()
                .nome("Usuario Test 2").email("usuario2@test.com").cpf("22222222222").sexo("F").senha("senha2").build();
        UsuarioEntity usuario3 = UsuarioEntity.builder()
                .nome("Usuario Test 3").email("usuario3@test.com").cpf("33333333333").sexo("M").senha("senha3").build();

        this.em.persist(usuario1);
        this.em.persist(usuario2);
        this.em.persist(usuario3);

        return List.of(usuario1, usuario2, usuario3);
    }
}
