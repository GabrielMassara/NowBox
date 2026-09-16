package com.nowbox.nowbox_api.security;

import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
public class UsuarioAutenticado {

    private final UUID id;
    private final Set<String> permissoes;
    private final Set<UUID> unidades;

    public UsuarioAutenticado(UUID id, Set<String> permissoes, Set<UUID> unidades) {
        this.id = id;
        this.permissoes = permissoes;
        this.unidades = unidades;
    }

}
