package com.nowbox.nowbox_api.modules.aluguel.repository;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
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
            AND (:status IS NULL OR a.status = :status)
            """)
    Page<AluguelEntity> findAllByFilter(@Param("idBox") UUID idBox,
                                        @Param("idCliente") UUID idCliente,
                                        @Param("status") StatusAluguel status,
                                        Pageable pageable);

    Optional<AluguelEntity> findByIdAndDeletedAtIsNull(UUID id);

    // Verifica se o box ja esta ocupado por algum aluguel, ou seja, com status diferente do informado (inativo)
    boolean existsByBoxIdAndStatusNotAndDeletedAtIsNull(UUID idBox, StatusAluguel status);

    // Box de origem de um aluguel que teve o box trocado e aguarda a assinatura do aditivo. Enquanto isso ele nao pode ser alugado
    boolean existsByBoxAnteriorIdAndStatusAndDeletedAtIsNull(UUID idBox, StatusAluguel status);

    boolean existsByBoxAnteriorIdAndStatusAndDeletedAtIsNullAndIdNot(UUID idBox, StatusAluguel status, UUID id);

    // Verifica se o box esta ocupado por algum aluguel alem do informado
    boolean existsByBoxIdAndStatusNotAndDeletedAtIsNullAndIdNot(UUID idBox, StatusAluguel status, UUID id);

}
