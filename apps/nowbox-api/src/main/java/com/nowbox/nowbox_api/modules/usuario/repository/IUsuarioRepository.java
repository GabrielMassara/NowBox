package com.nowbox.nowbox_api.modules.usuario.repository;

import com.nowbox.nowbox_api.modules.usuario.entity.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {

    @Query("""
            SELECT u FROM UsuarioEntity u
            WHERE u.deletedAt IS NULL
            AND (:nome IS NULL OR u.nome = :nome)
            AND (:email IS NULL OR u.email = :email)
            AND (:cpf IS NULL OR u.cpf = :cpf)
            """)
    Page<UsuarioEntity> findAllByFilter(@Param("nome") String nome,
                                        @Param("email") String email,
                                        @Param("cpf") String cpf,
                                        Pageable pageable);

    Optional<UsuarioEntity> findByIdAndDeletedAtIsNull(UUID id);

}
