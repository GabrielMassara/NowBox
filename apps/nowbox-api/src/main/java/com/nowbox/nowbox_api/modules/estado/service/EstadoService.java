package com.nowbox.nowbox_api.modules.estado.service;

import com.nowbox.nowbox_api.modules.estado.dto.EstadoResponseDTO;
import com.nowbox.nowbox_api.modules.estado.repository.IEstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstadoService {

    private final IEstadoRepository estadoRepository;

    public Page<EstadoResponseDTO> listAll(Pageable pageable) {
        return estadoRepository.findAll(pageable).map(e -> new EstadoResponseDTO(e.getId(), e.getNome(), e.getUf()));
    }

    public Optional<EstadoResponseDTO> find(UUID id) {
        return estadoRepository.findById(id).map(e -> new EstadoResponseDTO(e.getId(), e.getNome(), e.getUf()));
    }

}
