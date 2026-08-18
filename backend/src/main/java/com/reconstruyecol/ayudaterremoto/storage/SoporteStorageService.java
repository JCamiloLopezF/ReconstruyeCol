package com.reconstruyecol.ayudaterremoto.storage;

import org.springframework.web.multipart.MultipartFile;

public interface SoporteStorageService {

    /** Sube el archivo a almacenamiento restringido y devuelve la ruta (bucket/ruta), no una URL pública. */
    String subir(MultipartFile archivo, String rutaDestino);

    /**
     * Genera un enlace temporal (URL firmada) para que un administrador pueda ver un soporte
     * guardado en el bucket privado. {@code rutaAlmacenada} es el valor devuelto por {@link #subir}.
     */
    String generarUrlFirmada(String rutaAlmacenada, long expiracionSegundos);
}
