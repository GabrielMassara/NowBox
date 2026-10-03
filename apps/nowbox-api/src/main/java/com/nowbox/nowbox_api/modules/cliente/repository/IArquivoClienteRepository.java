package com.nowbox.nowbox_api.modules.cliente.repository;

import com.nowbox.nowbox_api.modules.cliente.entity.ArquivoClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.TipoDocumentoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IArquivoClienteRepository extends JpaRepository<ArquivoClienteEntity, UUID> {

    Page<ArquivoClienteEntity> findByClienteIdAndTipoOrderBySalvoEmDesc(UUID idCliente, TipoDocumentoCliente tipo, Pageable pageable);

    Optional<ArquivoClienteEntity> findByIdAndClienteId(UUID id, UUID idCliente);

}
