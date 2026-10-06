package com.nowbox.nowbox_api.modules.aluguel.repository;

import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardAluguelStatusDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardEvolucaoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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

    long countByBoxUnidadeIdAndStatusInAndDeletedAtIsNull(UUID idUnidade, Collection<StatusAluguel> status);

    @Query("""
            SELECT SUM(COALESCE(a.valorAnterior, a.valor)) FROM AluguelEntity a
            WHERE a.deletedAt IS NULL
            AND a.box.unidade.id = :idUnidade
            AND a.status IN :status
            """)
    BigDecimal sumValorVigenteByUnidade(@Param("idUnidade") UUID idUnidade,
                                        @Param("status") Collection<StatusAluguel> status);

    @Query("""
            SELECT COUNT(DISTINCT a.cliente.id) FROM AluguelEntity a
            WHERE a.deletedAt IS NULL
            AND a.box.unidade.id = :idUnidade
            AND a.status IN :status
            """)
    long countClientesByUnidadeAndStatusIn(@Param("idUnidade") UUID idUnidade,
                                           @Param("status") Collection<StatusAluguel> status);

    @Query("""
            SELECT new com.nowbox.nowbox_api.modules.dashboard.dto.DashboardAluguelStatusDTO(a.status, COUNT(a))
            FROM AluguelEntity a
            WHERE a.deletedAt IS NULL
            AND a.box.unidade.id = :idUnidade
            GROUP BY a.status
            """)
    List<DashboardAluguelStatusDTO> countByUnidadeGroupByStatus(@Param("idUnidade") UUID idUnidade);

    @Query("""
            SELECT new com.nowbox.nowbox_api.modules.dashboard.dto.DashboardEvolucaoDTO(YEAR(a.createdAt), MONTH(a.createdAt), COUNT(a), SUM(a.valor))
            FROM AluguelEntity a
            WHERE a.deletedAt IS NULL
            AND a.box.unidade.id = :idUnidade
            AND a.createdAt >= :desde
            GROUP BY YEAR(a.createdAt), MONTH(a.createdAt)
            """)
    List<DashboardEvolucaoDTO> countByUnidadeGroupByMes(@Param("idUnidade") UUID idUnidade,
                                                         @Param("desde") LocalDateTime desde);

    @Query("""
            SELECT a FROM AluguelEntity a
            WHERE a.deletedAt IS NULL
            AND a.box.unidade.id = :idUnidade
            AND a.status IN :status
            ORDER BY a.createdAt ASC
            """)
    List<AluguelEntity> findAllByUnidadeAndStatusIn(@Param("idUnidade") UUID idUnidade,
                                                    @Param("status") Collection<StatusAluguel> status,
                                                    Pageable pageable);

}
