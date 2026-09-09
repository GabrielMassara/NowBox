package com.nowbox.nowbox_api.modules.operacao.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import com.nowbox.nowbox_api.modules.modulo.repository.IModuloRepository;
import com.nowbox.nowbox_api.modules.operacao.dto.OperacaoCreateDTO;
import com.nowbox.nowbox_api.modules.operacao.dto.OperacaoFilterDTO;
import com.nowbox.nowbox_api.modules.operacao.dto.OperacaoResponseDTO;
import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import com.nowbox.nowbox_api.modules.operacao.repository.IOperacaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperacaoService {

    private final IOperacaoRepository operacaoRepository;
    private final IModuloRepository moduloRepository;

    public Page<OperacaoResponseDTO> listAllByFilter(Pageable pageable, OperacaoFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        String nome = null;
        String codigo = null;
        UUID idModulo = null;

        if(filtro != null) {
            if(StringUtils.hasText(filtro.getNome())) {
                nome = filtro.getNome();
            }
            if(StringUtils.hasText(filtro.getCodigo())) {
                codigo = filtro.getCodigo();
            }
            if(StringUtils.hasText(String.valueOf(filtro.getIdModulo()))) {
                idModulo = filtro.getIdModulo();
            }
        }

        return operacaoRepository.findAllByFilter(idModulo, nome, codigo, pageable).map(e -> OperacaoResponseDTO.builder().id(e.getId()).nome(e.getNome()).codigo(e.getCodigo()).modulo(e.getModulo()).build());
    }

    public OperacaoResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<OperacaoEntity> encontrado = operacaoRepository.findById(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Operação não encontrada");
        }

        return OperacaoResponseDTO.builder().id(encontrado.get().getId()).nome(encontrado.get().getNome()).codigo(encontrado.get().getCodigo()).modulo(encontrado.get().getModulo()).build();
    }

    @Transactional
    public OperacaoResponseDTO create(OperacaoCreateDTO operacao) throws NaoEncontradoException {

        Optional<ModuloEntity> modulo = moduloRepository.findById(operacao.getIdModulo());

        // Se não encontrar o modulo
        if(modulo.isEmpty()) {
            throw new NaoEncontradoException("Módulo inválido");
        }

        OperacaoEntity created = operacaoRepository.save(OperacaoEntity.builder().nome(operacao.getNome()).codigo(operacao.getCodigo()).modulo(modulo.get()).build());

        return OperacaoResponseDTO.builder().id(created.getId()).nome(created.getNome()).codigo(created.getCodigo()).modulo(created.getModulo()).build();
    }

    @Transactional
    public OperacaoResponseDTO update(OperacaoCreateDTO operacao, UUID id) throws NaoEncontradoException {
        //verifica se existe o registro
        if(operacaoRepository.findById(id).isEmpty()) {
            throw new NaoEncontradoException("Operação não encontrada");
        }

        // busca o modulo
        Optional<ModuloEntity> modulo = moduloRepository.findById(operacao.getIdModulo());
        if(modulo.isEmpty()) {
            throw new NaoEncontradoException("Módulo não encontrado");
        }

        OperacaoEntity updated = operacaoRepository.save(OperacaoEntity.builder().id(id).nome(operacao.getNome()).codigo(operacao.getCodigo()).modulo(modulo.get()).build());

        return OperacaoResponseDTO.builder().id(updated.getId()).nome(updated.getNome()).codigo(updated.getCodigo()).modulo(updated.getModulo()).build();
    }

    public void delete(UUID id) {
        operacaoRepository.deleteById(id);
    }
}
