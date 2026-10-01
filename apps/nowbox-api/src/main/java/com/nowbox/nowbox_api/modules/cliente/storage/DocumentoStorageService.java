package com.nowbox.nowbox_api.modules.cliente.storage;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class DocumentoStorageService {

    private final MinioClient minioClient;

    @Value("${app.storage.minio.bucket-documentos}")
    private String bucketDocumentos;

    public String getBucket() {
        return bucketDocumentos;
    }

    public void salvar(String chave, InputStream conteudo, long tamanho, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketDocumentos)
                    .object(chave)
                    .stream(conteudo, tamanho, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gravar o documento no armazenamento", e);
        }
    }

    public InputStream abrir(String chave) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketDocumentos).object(chave).build());
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new NaoEncontradoException("Documento não encontrado no armazenamento");
            }
            throw new IllegalStateException("Falha ao ler o documento no armazenamento", e);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao ler o documento no armazenamento", e);
        }
    }

    public void remover(String chave) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketDocumentos).object(chave).build());
        } catch (Exception ignorada) {
            // sem efeito para o usuario
        }
    }
}
