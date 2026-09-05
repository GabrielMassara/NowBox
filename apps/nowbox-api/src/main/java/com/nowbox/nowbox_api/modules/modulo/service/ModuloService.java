package com.nowbox.nowbox_api.modules.modulo.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.modulo.dto.ModuloCreateDTO;
import com.nowbox.nowbox_api.modules.modulo.dto.ModuloFilterDTO;
import com.nowbox.nowbox_api.modules.modulo.dto.ModuloResponseDTO;
import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import com.nowbox.nowbox_api.modules.modulo.repository.IModuloRepository;
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
public class ModuloService {

    private final IModuloRepository moduloRepository;
    private final ISessaoRepository sessaoRepository;

    public Page<ModuloResponseDTO> listAllByFilter(Pageable pageable, ModuloFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        String nome = null;
        String rota = null;
        UUID idSessao = null;

        if(filtro != null) {
            if(StringUtils.hasText(filtro.getNome())) {
                nome = filtro.getNome();
            }
            if(StringUtils.hasText(filtro.getRota())) {
                rota = filtro.getRota();
            }
            if(StringUtils.hasText(String.valueOf(filtro.getIdSessao()))) {
                idSessao = filtro.getIdSessao();
            }
        }

        return moduloRepository.findAllByFilter(idSessao, nome, rota, pageable).map(e -> ModuloResponseDTO.builder().id(e.getId()).nome(e.getNome()).rota(e.getRota()).sessao(e.getSessao()).build());
    }

    public ModuloResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<ModuloEntity> encontrado = moduloRepository.findById(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Modulo não encontrado");
        }

        return ModuloResponseDTO.builder().id(encontrado.get().getId()).nome(encontrado.get().getNome()).rota(encontrado.get().getRota()).sessao(encontrado.get().getSessao()).build();
    }

    @Transactional
    public ModuloResponseDTO create(ModuloCreateDTO modulo) throws NaoEncontradoException {

        Optional<SessaoEntity> sessao = sessaoRepository.findById(modulo.getIdSessao());

        // Se não encontrar a sessão
        if(sessao.isEmpty()) {
            throw new NaoEncontradoException("Sessão inválida");
        }

        ModuloEntity created = moduloRepository.save(ModuloEntity.builder().nome(modulo.getNome()).rota(modulo.getRota()).sessao(sessao.get()).build());

        return ModuloResponseDTO.builder().id(created.getId()).nome(created.getNome()).rota(created.getRota()).sessao(created.getSessao()).build();
    }

    @Transactional
    public ModuloResponseDTO update(ModuloCreateDTO modulo, UUID id) throws NaoEncontradoException {
        //verifica se existe o registro
        if(moduloRepository.findById(id).isEmpty()) {
            throw new NaoEncontradoException("Modulo não encontrado");
        }

        // busca a sessao
        Optional<SessaoEntity> sessao = sessaoRepository.findById(modulo.getIdSessao());
        if(sessao.isEmpty()) {
            throw new NaoEncontradoException("Sessão não encontrada");
        }

        ModuloEntity updated = moduloRepository.save(ModuloEntity.builder().id(id).nome(modulo.getNome()).rota(modulo.getRota()).sessao(sessao.get()).build());

        return ModuloResponseDTO.builder().id(updated.getId()).nome(updated.getNome()).rota(updated.getRota()).sessao(updated.getSessao()).build();
    }

    public void delete(UUID id) {
        moduloRepository.deleteById(id);
    }
}
