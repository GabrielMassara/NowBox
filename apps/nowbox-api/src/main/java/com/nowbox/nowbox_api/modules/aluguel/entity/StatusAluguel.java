package com.nowbox.nowbox_api.modules.aluguel.entity;


public enum StatusAluguel {
    PENDENTE_ASSINATURA_CONTRATO,
    ATIVO,
    PENDENTE_ASSINATURA_ADITIVO,
    PENDENTE_ASSINATURA_DISTRATO,
    INATIVO;

    // Enquanto nao estiver inativo o aluguel mantem o box ocupado
    public boolean ocupaBox() {
        return this != INATIVO;
    }
}
