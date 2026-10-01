package com.nowbox.nowbox_api.modules.contrato.repository;

import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoAluguelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

// tb_arquivo_aluguel guarda so os aditivos. O contrato original e referenciado direto em tb_aluguel
@Repository
public interface IArquivoAluguelRepository extends JpaRepository<ArquivoAluguelEntity, UUID> {

    // Historico de aditivos de um aluguel do mais recente para o mais antigo
    Page<ArquivoAluguelEntity> findByAluguelIdOrderBySalvoEmDesc(UUID idAluguel, Pageable pageable);

}
