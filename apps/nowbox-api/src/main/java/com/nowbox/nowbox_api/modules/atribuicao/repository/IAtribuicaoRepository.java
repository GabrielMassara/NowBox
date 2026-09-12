package com.nowbox.nowbox_api.modules.atribuicao.repository;

import com.nowbox.nowbox_api.modules.atribuicao.entity.AtribuicaoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IAtribuicaoRepository extends JpaRepository<AtribuicaoEntity, UUID> {

    @Query("""
            SELECT a FROM AtribuicaoEntity a
            WHERE a.deletedAt IS NULL
            AND (:idUsuario IS NULL OR a.usuario.id = :idUsuario)
            AND (:idCargo IS NULL OR a.cargo.id = :idCargo)
            """)
    Page<AtribuicaoEntity> findAllByFilter(@Param("idUsuario") UUID idUsuario,
                                           @Param("idCargo") UUID idCargo,
                                           Pageable pageable);

    Optional<AtribuicaoEntity> findByIdAndDeletedAtIsNull(UUID id);

}
