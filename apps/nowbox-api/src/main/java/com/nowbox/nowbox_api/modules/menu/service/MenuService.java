package com.nowbox.nowbox_api.modules.menu.service;

import com.nowbox.nowbox_api.modules.menu.dto.MenuModuloResponseDTO;
import com.nowbox.nowbox_api.modules.menu.dto.MenuSessaoResponseDTO;
import com.nowbox.nowbox_api.modules.modulo.entity.ModuloEntity;
import com.nowbox.nowbox_api.modules.modulo.repository.IModuloRepository;
import com.nowbox.nowbox_api.modules.sessao.entity.SessaoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final IModuloRepository moduloRepository;

    public List<MenuSessaoResponseDTO> listByUsuario(UUID idUsuario) {

        // Agrupa os modulos permitidos por sessao mantendo a ordem do repository
        Map<UUID, MenuSessaoResponseDTO> sessoes = new LinkedHashMap<>();
        for (ModuloEntity modulo : moduloRepository.findAllByUsuario(idUsuario)) {
            SessaoEntity sessao = modulo.getSessao();

            sessoes.computeIfAbsent(sessao.getId(), id -> MenuSessaoResponseDTO.builder()
                            .id(sessao.getId()).nome(sessao.getNome()).rota(sessao.getRota()).modulos(new ArrayList<>()).build())
                    .getModulos()
                    .add(MenuModuloResponseDTO.builder().id(modulo.getId()).nome(modulo.getNome()).rota(modulo.getRota()).build());
        }

        return List.copyOf(sessoes.values());
    }

}
