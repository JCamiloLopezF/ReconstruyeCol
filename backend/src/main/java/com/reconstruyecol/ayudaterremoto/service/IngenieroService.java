package com.reconstruyecol.ayudaterremoto.service;

import com.reconstruyecol.ayudaterremoto.common.EstadoVerificacion;
import com.reconstruyecol.ayudaterremoto.common.HashUtils;
import com.reconstruyecol.ayudaterremoto.mapper.IngenieroMapper;
import com.reconstruyecol.ayudaterremoto.model.Ingeniero;
import com.reconstruyecol.ayudaterremoto.model.dto.IngenieroPendienteResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.IngenieroRegistroRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.IngenieroRegistroResponse;
import com.reconstruyecol.ayudaterremoto.repository.IngenieroRepository;
import com.reconstruyecol.ayudaterremoto.storage.SoporteStorageService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class IngenieroService {

    private final IngenieroRepository ingenieroRepository;
    private final SoporteStorageService soporteStorageService;
    private final PasswordEncoder passwordEncoder;

    public IngenieroService(IngenieroRepository ingenieroRepository,
                             SoporteStorageService soporteStorageService,
                             PasswordEncoder passwordEncoder) {
        this.ingenieroRepository = ingenieroRepository;
        this.soporteStorageService = soporteStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public IngenieroRegistroResponse registrar(IngenieroRegistroRequest request) {
        if (ingenieroRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un ingeniero registrado con este correo");
        }

        String documentoHash = HashUtils.sha256(request.getDocumentoIdentidad());
        if (ingenieroRepository.existsByDocumentoIdentidadHash(documentoHash)) {
            throw new IllegalArgumentException("Ya existe un registro con este documento de identidad");
        }

        String rutaSoporte = "ingenieros/%s-%s".formatted(
                UUID.randomUUID(), sanitizarNombreArchivo(request.getSoporte().getOriginalFilename()));
        String urlSoporte = soporteStorageService.subir(request.getSoporte(), rutaSoporte);

        Ingeniero ingeniero = IngenieroMapper.toEntity(
                request, passwordEncoder.encode(request.getPassword()), documentoHash, urlSoporte);
        ingeniero = ingenieroRepository.save(ingeniero);

        return IngenieroMapper.toRegistroResponse(ingeniero);
    }

    private static String sanitizarNombreArchivo(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            return "soporte";
        }
        return nombreOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /** Genera un enlace firmado de 1 hora por cada soporte — así el admin nunca ve un link caducado. */
    @Transactional(readOnly = true)
    public List<IngenieroPendienteResponse> listarPendientes() {
        return ingenieroRepository.findByEstadoVerificacion(EstadoVerificacion.PENDIENTE).stream()
                .map(ingeniero -> IngenieroMapper.toPendienteResponse(
                        ingeniero, soporteStorageService.generarUrlFirmada(ingeniero.getUrlSoporte(), 3600)))
                .toList();
    }

    @Transactional
    public void actualizarEstado(UUID id, EstadoVerificacion nuevoEstado) {
        if (nuevoEstado == EstadoVerificacion.PENDIENTE) {
            throw new IllegalArgumentException("El estado debe ser VERIFICADO o RECHAZADO");
        }
        Ingeniero ingeniero = ingenieroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ingeniero no encontrado"));
        ingeniero.setEstadoVerificacion(nuevoEstado);
        ingenieroRepository.save(ingeniero);
    }
}
