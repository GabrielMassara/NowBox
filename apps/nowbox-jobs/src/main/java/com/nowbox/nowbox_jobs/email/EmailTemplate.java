package com.nowbox.nowbox_jobs.email;

public enum EmailTemplate {

    ALUGUEL_REGISTRADO("aluguel-registrado");

    private final String arquivo;

    EmailTemplate(String arquivo) {
        this.arquivo = arquivo;
    }

    public String getArquivo() {
        return arquivo;
    }
}
