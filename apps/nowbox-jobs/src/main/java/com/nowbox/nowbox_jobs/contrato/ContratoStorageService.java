package com.nowbox.nowbox_jobs.contrato;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

// Grava o PDF no bucket privado de contratos
@Service
@RequiredArgsConstructor
public class ContratoStorageService {

    private final MinioClient minioClient;

    @Value("${app.storage.minio.bucket-contratos}")
    private String bucketContratos;

    public String getBucketContratos() {
        return bucketContratos;
    }

    public void gravar(String chave, byte[] conteudo, String contentType) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(conteudo)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketContratos)
                    .object(chave)
                    .stream(stream, conteudo.length, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gravar o contrato no armazenamento: " + chave, e);
        }
    }
}
