package com.nowbox.nowbox_api.modules.modulo.repository;

import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IModuloRepository extends JpaRepository<ModuloEntity, UUID> {

    @Query("""
            SELECT m FROM ModuloEntity m
            WHERE (:idSessao IS NULL OR m.sessao.id = :idSessao)
            AND (:nome IS NULL OR m.nome = :nome)
            AND (:rota IS NULL OR m.rota = :rota)
            """)
    Page<ModuloEntity> findAllByFilter(@Param("idSessao") UUID idSessao,
                                        @Param("nome") String nome,
                                        @Param("rota") String rota,
                                        Pageable pageable);

}
