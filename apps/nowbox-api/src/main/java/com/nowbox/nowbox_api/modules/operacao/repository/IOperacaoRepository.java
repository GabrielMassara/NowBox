package com.nowbox.nowbox_api.modules.operacao.repository;

import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IOperacaoRepository extends JpaRepository<OperacaoEntity, UUID> {

    @Query("""
            SELECT o FROM OperacaoEntity o
            WHERE (:idModulo IS NULL OR o.modulo.id = :idModulo)
            AND (:nome IS NULL OR o.nome = :nome)
            AND (:codigo IS NULL OR o.codigo = :codigo)
            """)
    Page<OperacaoEntity> findAllByFilter(@Param("idModulo") UUID idModulo,
                                          @Param("nome") String nome,
                                          @Param("codigo") String codigo,
                                          Pageable pageable);

}
