package com.nowbox.nowbox_api.modules.unidade.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.estado.repository.IEstadoRepository;
import com.nowbox.nowbox_api.modules.unidade.dto.UnidadeCreateDTO;
import com.nowbox.nowbox_api.modules.unidade.dto.UnidadeFilterDTO;
import com.nowbox.nowbox_api.modules.unidade.dto.UnidadeResponseDTO;
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
public class UnidadeService {

    private final IUnidadeRepository unidadeRepository;
    private final IEstadoRepository estadoRepository;

    public Page<UnidadeResponseDTO> listAllByFilter(Pageable pageable, UnidadeFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        String nome = null;
        String cnpj = null;
        String cidade = null;
        UUID idEstado = null;

        if(filtro != null) {
            if(StringUtils.hasText(filtro.getNome())) {
                nome = filtro.getNome();
            }
            if(StringUtils.hasText(filtro.getCnpj())) {
                cnpj = filtro.getCnpj();
            }
            if(StringUtils.hasText(filtro.getCidade())) {
                cidade = filtro.getCidade();
            }
            if(StringUtils.hasText(String.valueOf(filtro.getIdEstado()))) {
                idEstado = filtro.getIdEstado();
            }
        }

        return unidadeRepository.findAllByFilter(idEstado, nome, cnpj, cidade, pageable).map(this::toResponseDTO);
    }

    public UnidadeResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<UnidadeEntity> encontrado = unidadeRepository.findByIdAndDeletedAtIsNull(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Unidade não encontrada");
        }

        return toResponseDTO(encontrado.get());
    }

    @Transactional
    public UnidadeResponseDTO create(UnidadeCreateDTO unidade) throws NaoEncontradoException {

        Optional<EstadoEntity> estado = estadoRepository.findById(unidade.getIdEstado());

        // Se não encontrar o estado
        if(estado.isEmpty()) {
            throw new NaoEncontradoException("Estado inválido");
        }

        UnidadeEntity created = unidadeRepository.save(UnidadeEntity.builder()
                .nome(unidade.getNome())
                .cnpj(unidade.getCnpj())
                .endereco(unidade.getEndereco())
                .numero(unidade.getNumero())
                .complemento(unidade.getComplemento())
                .bairro(unidade.getBairro())
                .cep(unidade.getCep())
                .cidade(unidade.getCidade())
                .estado(estado.get())
                .build());

        return toResponseDTO(created);
    }

    @Transactional
    public UnidadeResponseDTO update(UnidadeCreateDTO unidade, UUID id) throws NaoEncontradoException {
        // verifica se existe o registro
        Optional<UnidadeEntity> existente = unidadeRepository.findByIdAndDeletedAtIsNull(id);
        if(existente.isEmpty()) {
            throw new NaoEncontradoException("Unidade não encontrada");
        }

        // busca o estado
        Optional<EstadoEntity> estado = estadoRepository.findById(unidade.getIdEstado());
        if(estado.isEmpty()) {
            throw new NaoEncontradoException("Estado não encontrado");
        }

        UnidadeEntity updated = unidadeRepository.save(UnidadeEntity.builder()
                .id(id)
                .nome(unidade.getNome())
                .cnpj(unidade.getCnpj())
                .endereco(unidade.getEndereco())
                .numero(unidade.getNumero())
                .complemento(unidade.getComplemento())
                .bairro(unidade.getBairro())
                .cep(unidade.getCep())
                .cidade(unidade.getCidade())
                .estado(estado.get())
                .createdAt(existente.get().getCreatedAt())
                .build());

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(UUID id) throws NaoEncontradoException {
        UnidadeEntity existente = unidadeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Unidade não encontrada"));

        existente.setDeletedAt(LocalDateTime.now());
        unidadeRepository.save(existente);
    }

    private UnidadeResponseDTO toResponseDTO(UnidadeEntity entidade) {
        return UnidadeResponseDTO.builder()
                .id(entidade.getId())
                .nome(entidade.getNome())
                .cnpj(entidade.getCnpj())
                .endereco(entidade.getEndereco())
                .numero(entidade.getNumero())
                .complemento(entidade.getComplemento())
                .bairro(entidade.getBairro())
                .cep(entidade.getCep())
                .cidade(entidade.getCidade())
                .estado(entidade.getEstado())
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }
}
