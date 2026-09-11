package com.nowbox.nowbox_api.modules.permissao.repository;

import com.nowbox.nowbox_api.modules.permissao.entity.PermissaoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IPermissaoRepository extends JpaRepository<PermissaoEntity, UUID> {

    @Query("""
            SELECT p FROM PermissaoEntity p
            WHERE (:idCargo IS NULL OR p.cargo.id = :idCargo)
            AND (:idOperacao IS NULL OR p.operacao.id = :idOperacao)
            """)
    Page<PermissaoEntity> findAllByFilter(@Param("idCargo") UUID idCargo,
                                           @Param("idOperacao") UUID idOperacao,
                                           Pageable pageable);

}
