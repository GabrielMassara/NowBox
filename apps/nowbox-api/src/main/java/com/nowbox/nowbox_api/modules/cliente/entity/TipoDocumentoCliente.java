package com.nowbox.nowbox_api.modules.cliente.entity;

public enum TipoDocumentoCliente {
    IDENTIDADE("documento de identidade", "clientes/documentos-identidade/"),
    COMPROVANTE_RESIDENCIA("comprovante de residência", "clientes/comprovantes-residencia/");

    private final String descricao;
    private final String prefixoChave;

    TipoDocumentoCliente(String descricao, String prefixoChave) {
        this.descricao = descricao;
        this.prefixoChave = prefixoChave;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getPrefixoChave() {
        return prefixoChave;
    }
}
