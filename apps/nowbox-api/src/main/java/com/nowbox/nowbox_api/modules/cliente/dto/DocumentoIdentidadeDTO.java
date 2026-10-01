package com.nowbox.nowbox_api.modules.cliente.dto;

import java.util.UUID;

public record DocumentoIdentidadeDTO(UUID id, String nomeArquivo, String contentType, Long tamanho) {
}
