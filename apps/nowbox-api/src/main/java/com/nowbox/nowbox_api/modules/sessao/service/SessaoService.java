package com.nowbox.nowbox_api.modules.sessao.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.sessao.dto.SessaoCreateDTO;
import com.nowbox.nowbox_api.modules.sessao.dto.SessaoRequestFilterDTO;
import com.nowbox.nowbox_api.modules.sessao.dto.SessaoResponseDTO;
import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
import com.nowbox.nowbox_api.modules.sessao.repository.ISessaoRepository;
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
public class SessaoService {

    private final ISessaoRepository sessaoRepository;

    public Page<SessaoResponseDTO> listAll(Pageable pageable, SessaoRequestFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        String nome = null;
        String rota = null;
        if(filtro != null) {
            if(StringUtils.hasText(filtro.getNome())) {
                nome = filtro.getNome();
            }
            if(StringUtils.hasText(filtro.getRota())) {
                rota = filtro.getRota();
            }
        }

        return sessaoRepository.findAllByFiltro(nome, rota, pageable)
                .map(e -> SessaoResponseDTO.builder().id(e.getId()).nome(e.getNome()).rota(e.getRota()).build());
    }

    public Optional<SessaoResponseDTO> listById(UUID id) {
        return sessaoRepository.findById(id).map(e -> SessaoResponseDTO.builder().id(e.getId()).nome(e.getNome()).rota(e.getRota()).build());
    }

    public SessaoResponseDTO save(SessaoCreateDTO sessao) {
        SessaoEntity entidade = sessaoRepository.save(SessaoEntity.builder().nome(sessao.getNome()).rota(sessao.getRota()).build());
        return SessaoResponseDTO.builder().id(entidade.getId()).nome(entidade.getNome()).rota(entidade.getRota()).build();
    }

    @Transactional
    public void update(UUID id, SessaoCreateDTO sessao) {
        sessaoRepository.findById(id).orElseThrow(() -> new NaoEncontradoException("Sessao não encontrada."));
        sessaoRepository.save(SessaoEntity.builder().id(id).nome(sessao.getNome()).rota(sessao.getRota()).build());
    }

    @Transactional
    public void delete(UUID id) {
        sessaoRepository.findById(id).orElseThrow(() -> new NaoEncontradoException("Sessao não encontrada."));
        sessaoRepository.deleteById(id);
    }

}
