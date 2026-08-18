package com.reconstruyecol.ayudaterremoto.storage;

import org.springframework.web.multipart.MultipartFile;

public interface SoporteStorageService {

    /** Sube el archivo a almacenamiento restringido y devuelve la ruta (bucket/ruta), no una URL pública. */
    String subir(MultipartFile archivo, String rutaDestino);
}
