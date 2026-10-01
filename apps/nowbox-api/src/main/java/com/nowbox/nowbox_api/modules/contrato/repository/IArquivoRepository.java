package com.nowbox.nowbox_api.modules.contrato.repository;

import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IArquivoRepository extends JpaRepository<ArquivoEntity, UUID> {
}
