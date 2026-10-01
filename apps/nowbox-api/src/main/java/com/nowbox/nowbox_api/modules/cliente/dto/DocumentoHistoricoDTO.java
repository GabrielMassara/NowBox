package com.nowbox.nowbox_api.modules.cliente.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoHistoricoDTO(UUID id, String nomeArquivo, String contentType, Long tamanho, LocalDateTime salvoEm, boolean atual) {
}
