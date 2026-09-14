package com.nowbox.nowbox_api.modules.box.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.box.dto.BoxCreateDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxFilterDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxResponseDTO;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import com.nowbox.nowbox_api.modules.unidade.repository.IUnidadeRepository;
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
public class BoxService {

    private final IBoxRepository boxRepository;
    private final IUnidadeRepository unidadeRepository;

    public Page<BoxResponseDTO> listAllByFilter(Pageable pageable, BoxFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        UUID idUnidade = null;
        String numero = null;
        Boolean disponivel = null;

        if(filtro != null) {
            if(StringUtils.hasText(String.valueOf(filtro.getIdUnidade()))) {
                idUnidade = filtro.getIdUnidade();
            }
            if(StringUtils.hasText(filtro.getNumero())) {
                numero = filtro.getNumero();
            }
            if(filtro.getDisponivel() != null) {
                disponivel = filtro.getDisponivel();
            }
        }

        return boxRepository.findAllByFilter(idUnidade, numero, disponivel, pageable).map(this::toResponseDTO);
    }

    public BoxResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<BoxEntity> encontrado = boxRepository.findByIdAndDeletedAtIsNull(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Box não encontrado");
        }

        return toResponseDTO(encontrado.get());
    }

    @Transactional
    public BoxResponseDTO create(BoxCreateDTO box) throws NaoEncontradoException {

        Optional<UnidadeEntity> unidade = unidadeRepository.findByIdAndDeletedAtIsNull(box.getIdUnidade());

        // Se não encontrar a unidade
        if(unidade.isEmpty()) {
            throw new NaoEncontradoException("Unidade inválida");
        }

        BoxEntity created = boxRepository.save(BoxEntity.builder()
                .numero(box.getNumero())
                .unidade(unidade.get())
                .tamanho(box.getTamanho())
                .dimensoes(box.getDimensoes())
                .disponivel(box.getDisponivel())
                .preco(box.getPreco())
                .build());

        return toResponseDTO(created);
    }

    @Transactional
    public BoxResponseDTO update(BoxCreateDTO box, UUID id) throws NaoEncontradoException {
        // verifica se existe o registro
        Optional<BoxEntity> existente = boxRepository.findByIdAndDeletedAtIsNull(id);
        if(existente.isEmpty()) {
            throw new NaoEncontradoException("Box não encontrado");
        }

        // busca a unidade
        Optional<UnidadeEntity> unidade = unidadeRepository.findByIdAndDeletedAtIsNull(box.getIdUnidade());
        if(unidade.isEmpty()) {
            throw new NaoEncontradoException("Unidade não encontrada");
        }

        BoxEntity updated = boxRepository.save(BoxEntity.builder()
                .id(id)
                .numero(box.getNumero())
                .unidade(unidade.get())
                .tamanho(box.getTamanho())
                .dimensoes(box.getDimensoes())
                .disponivel(box.getDisponivel())
                .preco(box.getPreco())
                .createdAt(existente.get().getCreatedAt())
                .build());

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(UUID id) throws NaoEncontradoException {
        BoxEntity existente = boxRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Box não encontrado"));

        existente.setDeletedAt(LocalDateTime.now());
        boxRepository.save(existente);
    }

    private BoxResponseDTO toResponseDTO(BoxEntity entidade) {
        return BoxResponseDTO.builder()
                .id(entidade.getId())
                .numero(entidade.getNumero())
                .unidade(entidade.getUnidade())
                .tamanho(entidade.getTamanho())
                .dimensoes(entidade.getDimensoes())
                .disponivel(entidade.getDisponivel())
                .preco(entidade.getPreco())
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }
}
