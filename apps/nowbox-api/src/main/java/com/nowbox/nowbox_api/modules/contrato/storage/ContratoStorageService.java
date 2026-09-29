package com.nowbox.nowbox_api.modules.contrato.storage;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

// Acesso ao bucket privado de contratos. A API é a unica que le o bucket e so entrega o arquivo depois de validar a permissao do usuario
@Service
@RequiredArgsConstructor
public class ContratoStorageService {

    private final MinioClient minioClient;

    @Value("${app.storage.minio.bucket-contratos}")
    private String bucketContratos;

    public InputStream abrir(String chave) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketContratos).object(chave).build());
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new NaoEncontradoException("Arquivo do contrato não encontrado no armazenamento");
            }
            throw new IllegalStateException("Falha ao ler o contrato no armazenamento", e);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao ler o contrato no armazenamento", e);
        }
    }
}
