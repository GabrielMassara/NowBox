package com.nowbox.nowbox_api.modules.box.repository;

import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IBoxRepository extends JpaRepository<BoxEntity, UUID> {

    @Query("""
            SELECT b FROM BoxEntity b
            WHERE b.deletedAt IS NULL
            AND (:idUnidade IS NULL OR b.unidade.id = :idUnidade)
            AND (:numero IS NULL OR b.numero = :numero)
            AND (:disponivel IS NULL OR b.disponivel = :disponivel)
            """)
    Page<BoxEntity> findAllByFilter(@Param("idUnidade") UUID idUnidade,
                                     @Param("numero") String numero,
                                     @Param("disponivel") Boolean disponivel,
                                     Pageable pageable);

    Optional<BoxEntity> findByIdAndDeletedAtIsNull(UUID id);

}
