package com.nowbox.nowbox_api.security;

import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

//Restringe o acesso de box e aluguel as unidades para as quais o usuario autenticado possui um cargo
@Component
@RequiredArgsConstructor
public class AcessoUnidadeService {

    private final IBoxRepository boxRepository;
    private final IAluguelRepository aluguelRepository;

    public boolean temAcesso(UUID idUnidade) {
        if (idUnidade == null) {
            return false;
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UsuarioAutenticado usuarioAutenticado)) {
            return false;
        }

        return usuarioAutenticado.getUnidades().contains(idUnidade);
    }

    public boolean temAcessoBox(UUID idBox) {
        if (idBox == null) {
            return false;
        }

        return boxRepository.findByIdAndDeletedAtIsNull(idBox)
                .map(box -> temAcesso(box.getUnidade().getId()))
                .orElse(true);
    }

    public boolean temAcessoAluguel(UUID idAluguel) {
        if (idAluguel == null) {
            return false;
        }

        return aluguelRepository.findByIdAndDeletedAtIsNull(idAluguel)
                .map(aluguel -> temAcesso(aluguel.getBox().getUnidade().getId()))
                .orElse(true);
    }

}
