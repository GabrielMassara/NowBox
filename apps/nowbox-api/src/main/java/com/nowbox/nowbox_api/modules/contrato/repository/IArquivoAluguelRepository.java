package com.nowbox.nowbox_api.modules.contrato.repository;

import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoAluguelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IArquivoAluguelRepository extends JpaRepository<ArquivoAluguelEntity, UUID> {

    // Historico de contratos de um aluguel do mais recente para o mais antigo
    Page<ArquivoAluguelEntity> findByAluguelIdOrderBySalvoEmDesc(UUID idAluguel, Pageable pageable);

    // Historico de contratos de todos os alugueis de um box do mais recente para o mais antigo
    Page<ArquivoAluguelEntity> findByBoxIdOrderBySalvoEmDesc(UUID idBox, Pageable pageable);

    // O contrato atual é o ultimo gerado
    Optional<ArquivoAluguelEntity> findFirstByAluguelIdOrderBySalvoEmDesc(UUID idAluguel);

}
