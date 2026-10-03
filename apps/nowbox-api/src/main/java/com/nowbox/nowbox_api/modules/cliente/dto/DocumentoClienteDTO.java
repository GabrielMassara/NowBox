package com.nowbox.nowbox_api.modules.cliente.dto;

import java.util.UUID;

public record DocumentoClienteDTO(UUID id, String nomeArquivo, String contentType, Long tamanho) {
}
