package com.nowbox.nowbox_api.modules.estado.repository;

import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IEstadoRepository extends JpaRepository<EstadoEntity, UUID> {
}
