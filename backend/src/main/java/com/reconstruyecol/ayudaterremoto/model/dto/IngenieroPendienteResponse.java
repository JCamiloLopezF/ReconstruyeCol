package com.reconstruyecol.ayudaterremoto.model.dto;

import com.reconstruyecol.ayudaterremoto.common.EstadoVerificacion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class IngenieroPendienteResponse {

    private final UUID id;
    private final String nombre;
    private final String email;
    private final String universidad;
    private final LocalDate fechaGraduacion;
    private final String urlSoporte;
    private final EstadoVerificacion estadoVerificacion;
    private final Instant createdAt;

    public IngenieroPendienteResponse(UUID id, String nombre, String email, String universidad,
                                       LocalDate fechaGraduacion, String urlSoporte,
                                       EstadoVerificacion estadoVerificacion, Instant createdAt) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.universidad = universidad;
        this.fechaGraduacion = fechaGraduacion;
        this.urlSoporte = urlSoporte;
        this.estadoVerificacion = estadoVerificacion;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getUniversidad() {
        return universidad;
    }

    public LocalDate getFechaGraduacion() {
        return fechaGraduacion;
    }

    public String getUrlSoporte() {
        return urlSoporte;
    }

    public EstadoVerificacion getEstadoVerificacion() {
        return estadoVerificacion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
