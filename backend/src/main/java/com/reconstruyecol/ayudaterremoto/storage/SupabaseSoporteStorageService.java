package com.reconstruyecol.ayudaterremoto.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Sube los soportes de ingenieros al bucket privado de Supabase Storage (mismo proyecto que la base
 * de datos). Requiere que el bucket ya exista y sea privado (ver docs/03-despliegue.md).
 *
 * Usa el nuevo sistema de API keys de Supabase (secret key, formato sb_secret_...): la key va en el
 * header "apikey", NUNCA en "Authorization: Bearer" (con ese header, la plataforma intenta parsearla
 * como JWT y la rechaza) — ver guia de migracion de Supabase.
 */
@Service
public class SupabaseSoporteStorageService implements SoporteStorageService {

    private final RestClient restClient;
    private final String bucket;

    public SupabaseSoporteStorageService(
            @Value("${app.supabase.url:}") String supabaseUrl,
            @Value("${app.supabase.secret-key:}") String secretKey,
            @Value("${app.supabase.storage-bucket:soportes-ingenieros}") String bucket) {
        this.bucket = bucket;
        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl + "/storage/v1")
                .defaultHeader("apikey", secretKey)
                .build();
    }

    @Override
    public String subir(MultipartFile archivo, String rutaDestino) {
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo leer el archivo de soporte", e);
        }

        String contentType = archivo.getContentType() != null
                ? archivo.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        try {
            restClient.post()
                    .uri("/object/{bucket}/{ruta}", bucket, rutaDestino)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(contenido)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new AlmacenamientoException("No se pudo subir el soporte al almacenamiento", e);
        }

        return bucket + "/" + rutaDestino;
    }
}
