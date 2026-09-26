package com.nowbox.nowbox_api.modules.aluguel.service;

import com.nowbox.nowbox_api.common.exception.ConflitoException;
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
        Boolean status = null;

        if(filtro != null) {
            if(StringUtils.hasText(String.valueOf(filtro.getIdBox()))) {
                idBox = filtro.getIdBox();
            }
            if(StringUtils.hasText(String.valueOf(filtro.getIdCliente()))) {
                idCliente = filtro.getIdCliente();
            }
            if(filtro.getStatus() != null) {
                status = filtro.getStatus();
            }
        }

        return aluguelRepository.findAllByFilter(idBox, idCliente, status, pageable).map(this::toResponseDTO);
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

        // Um box bloqueado nao pode ser alugado
        if(Boolean.TRUE.equals(aluguel.getStatus())) {
            validarBoxLiberado(box.get());
        }

        // Um box so pode ter um aluguel ativo por vez
        if(Boolean.TRUE.equals(aluguel.getStatus()) && aluguelRepository.existsByBoxIdAndStatusTrueAndDeletedAtIsNull(aluguel.getIdBox())) {
            throw new ConflitoException("O box já possui um aluguel ativo");
        }

        AluguelEntity created = aluguelRepository.save(AluguelEntity.builder()
                .box(box.get())
                .cliente(cliente.get())
                .valor(aluguel.getValor())
                .observacao(aluguel.getObservacao())
                .status(aluguel.getStatus())
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

        // Um box bloqueado nao pode ser alugado. so valida quando o aluguel passa a ficar ativo neste box, para que o bloqueio de um box ja alugado nao impeça editar o aluguel que ja estava ativo nele
        AluguelEntity atual = existente.get();
        boolean jaAtivoNoBox = Boolean.TRUE.equals(atual.getStatus()) && atual.getBox() != null && aluguel.getIdBox().equals(atual.getBox().getId());
        if(Boolean.TRUE.equals(aluguel.getStatus()) && !jaAtivoNoBox) {
            validarBoxLiberado(box.get());
        }

        // Um box so pode ter um aluguel ativo por vez, desconsiderando o proprio aluguel
        if(Boolean.TRUE.equals(aluguel.getStatus()) && aluguelRepository.existsByBoxIdAndStatusTrueAndDeletedAtIsNullAndIdNot(aluguel.getIdBox(), id)) {
            throw new ConflitoException("O box já possui um aluguel ativo");
        }

        AluguelEntity updated = aluguelRepository.save(AluguelEntity.builder()
                .id(id)
                .box(box.get())
                .cliente(cliente.get())
                .valor(aluguel.getValor())
                .observacao(aluguel.getObservacao())
                .status(aluguel.getStatus())
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

    // Disponivel false indica que o box esta bloqueado para locacao
    private void validarBoxLiberado(BoxEntity box) {
        if(Boolean.FALSE.equals(box.getDisponivel())) {
            throw new ConflitoException("O box está bloqueado para locação");
        }
    }

    private AluguelResponseDTO toResponseDTO(AluguelEntity entidade) {
        return AluguelResponseDTO.builder()
                .id(entidade.getId())
                .box(entidade.getBox())
                .cliente(entidade.getCliente())
                .valor(entidade.getValor())
                .observacao(entidade.getObservacao())
                .status(entidade.getStatus())
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }
}
