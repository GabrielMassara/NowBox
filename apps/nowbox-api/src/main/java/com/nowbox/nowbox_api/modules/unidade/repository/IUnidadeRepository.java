package com.nowbox.nowbox_api.modules.unidade.repository;

import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUnidadeRepository extends JpaRepository<UnidadeEntity, UUID> {

    @Query("""
            SELECT u FROM UnidadeEntity u
            WHERE u.deletedAt IS NULL
            AND (:idEstado IS NULL OR u.estado.id = :idEstado)
            AND (:nome IS NULL OR u.nome = :nome)
            AND (:cnpj IS NULL OR u.cnpj = :cnpj)
            AND (:cidade IS NULL OR u.cidade = :cidade)
            """)
    Page<UnidadeEntity> findAllByFilter(@Param("idEstado") UUID idEstado,
                                        @Param("nome") String nome,
                                        @Param("cnpj") String cnpj,
                                        @Param("cidade") String cidade,
                                        Pageable pageable);

    Optional<UnidadeEntity> findByIdAndDeletedAtIsNull(UUID id);

}
