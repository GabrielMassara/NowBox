package com.nowbox.nowbox_api.modules.cliente.dto;

import java.io.InputStream;

public record DocumentoDownloadDTO(String nomeArquivo, String contentType, Long tamanho, InputStream conteudo) {
}
