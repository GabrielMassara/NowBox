package com.nowbox.nowbox_api.modules.aluguel.repository;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IAluguelRepository extends JpaRepository<AluguelEntity, UUID> {

    @Query("""
            SELECT a FROM AluguelEntity a
            WHERE a.deletedAt IS NULL
            AND (:idBox IS NULL OR a.box.id = :idBox)
            AND (:idCliente IS NULL OR a.cliente.id = :idCliente)
            """)
    Page<AluguelEntity> findAllByFilter(@Param("idBox") UUID idBox,
                                        @Param("idCliente") UUID idCliente,
                                        Pageable pageable);

    Optional<AluguelEntity> findByIdAndDeletedAtIsNull(UUID id);

}
