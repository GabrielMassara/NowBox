package com.nowbox.nowbox_api.modules.cargo.repository;

import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ICargoRepository extends JpaRepository<CargoEntity, UUID> {

    @Query("""
            SELECT c FROM CargoEntity c
            WHERE (:idUnidade IS NULL OR c.unidade.id = :idUnidade)
            AND (:nome IS NULL OR c.nome = :nome)
            """)
    Page<CargoEntity> findAllByFilter(@Param("idUnidade") UUID idUnidade,
                                       @Param("nome") String nome,
                                       Pageable pageable);

}
