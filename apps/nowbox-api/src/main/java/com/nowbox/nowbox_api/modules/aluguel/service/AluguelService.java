package com.nowbox.nowbox_api.modules.aluguel.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelCreateDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelFilterDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelResponseDTO;
import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.repository.IClienteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AluguelService {

    private final IAluguelRepository aluguelRepository;
    private final IBoxRepository boxRepository;
    private final IClienteRepository clienteRepository;

    public Page<AluguelResponseDTO> listAllByFilter(Pageable pageable, AluguelFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        UUID idBox = null;
        UUID idCliente = null;

        if(filtro != null) {
            if(StringUtils.hasText(String.valueOf(filtro.getIdBox()))) {
                idBox = filtro.getIdBox();
            }
            if(StringUtils.hasText(String.valueOf(filtro.getIdCliente()))) {
                idCliente = filtro.getIdCliente();
            }
        }

        return aluguelRepository.findAllByFilter(idBox, idCliente, pageable).map(this::toResponseDTO);
    }

    public AluguelResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<AluguelEntity> encontrado = aluguelRepository.findByIdAndDeletedAtIsNull(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Aluguel não encontrado");
        }

        return toResponseDTO(encontrado.get());
    }

    @Transactional
    public AluguelResponseDTO create(AluguelCreateDTO aluguel) throws NaoEncontradoException {

        Optional<BoxEntity> box = boxRepository.findByIdAndDeletedAtIsNull(aluguel.getIdBox());

        // Se não encontrar o box
        if(box.isEmpty()) {
            throw new NaoEncontradoException("Box inválido");
        }

        Optional<ClienteEntity> cliente = clienteRepository.findByIdAndDeletedAtIsNull(aluguel.getIdCliente());

        // Se não encontrar o cliente
        if(cliente.isEmpty()) {
            throw new NaoEncontradoException("Cliente inválido");
        }

        AluguelEntity created = aluguelRepository.save(AluguelEntity.builder()
                .box(box.get())
                .cliente(cliente.get())
                .valor(aluguel.getValor())
                .observacao(aluguel.getObservacao())
                .build());

        return toResponseDTO(created);
    }

    @Transactional
    public AluguelResponseDTO update(AluguelCreateDTO aluguel, UUID id) throws NaoEncontradoException {
        // verifica se existe o registro
        Optional<AluguelEntity> existente = aluguelRepository.findByIdAndDeletedAtIsNull(id);
        if(existente.isEmpty()) {
            throw new NaoEncontradoException("Aluguel não encontrado");
        }

        // busca o box
        Optional<BoxEntity> box = boxRepository.findByIdAndDeletedAtIsNull(aluguel.getIdBox());
        if(box.isEmpty()) {
            throw new NaoEncontradoException("Box não encontrado");
        }

        // busca o cliente
        Optional<ClienteEntity> cliente = clienteRepository.findByIdAndDeletedAtIsNull(aluguel.getIdCliente());
        if(cliente.isEmpty()) {
            throw new NaoEncontradoException("Cliente não encontrado");
        }

        AluguelEntity updated = aluguelRepository.save(AluguelEntity.builder()
                .id(id)
                .box(box.get())
                .cliente(cliente.get())
                .valor(aluguel.getValor())
                .observacao(aluguel.getObservacao())
                .createdAt(existente.get().getCreatedAt())
                .build());

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(UUID id) throws NaoEncontradoException {
        AluguelEntity existente = aluguelRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        existente.setDeletedAt(LocalDateTime.now());
        aluguelRepository.save(existente);
    }

    private AluguelResponseDTO toResponseDTO(AluguelEntity entidade) {
        return AluguelResponseDTO.builder()
                .id(entidade.getId())
                .box(entidade.getBox())
                .cliente(entidade.getCliente())
                .valor(entidade.getValor())
                .observacao(entidade.getObservacao())
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }
}
