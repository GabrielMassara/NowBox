package com.nowbox.nowbox_api.modules.sessao.repository;

import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ISessaoRepository extends JpaRepository<SessaoEntity, UUID> {

    @Query("""
            SELECT s FROM SessaoEntity s
            WHERE (:nome IS NULL OR s.nome = :nome)
            AND (:rota IS NULL OR s.rota = :rota)
            """)
    Page<SessaoEntity> findAllByFiltro(@Param("nome") String nome, @Param("rota") String rota, Pageable pageable);

}
