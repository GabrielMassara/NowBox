package com.nowbox.nowbox_api.modules.cliente.repository;

import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IClienteRepository extends JpaRepository<ClienteEntity, UUID> {

    @Query("""
            SELECT c FROM ClienteEntity c
            WHERE c.deletedAt IS NULL
            AND (:idEstado IS NULL OR c.estado.id = :idEstado)
            AND (:nome IS NULL OR c.nome = :nome)
            AND (:cpf IS NULL OR c.cpf = :cpf)
            AND (:email IS NULL OR c.email = :email)
            """)
    Page<ClienteEntity> findAllByFilter(@Param("idEstado") UUID idEstado,
                                        @Param("nome") String nome,
                                        @Param("cpf") String cpf,
                                        @Param("email") String email,
                                        Pageable pageable);

    Optional<ClienteEntity> findByIdAndDeletedAtIsNull(UUID id);

}
