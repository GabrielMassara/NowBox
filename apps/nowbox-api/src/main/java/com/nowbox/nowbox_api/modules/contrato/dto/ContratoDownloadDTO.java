package com.nowbox.nowbox_api.modules.contrato.dto;

import java.io.InputStream;

// Conteudo de um contrato pronto para ser enviado ao cliente
public record ContratoDownloadDTO(String nomeArquivo, String contentType, Long tamanho, InputStream conteudo) {
}
