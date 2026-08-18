package com.reconstruyecol.ayudaterremoto.model.dto;

import com.reconstruyecol.ayudaterremoto.common.EstadoVerificacion;

import java.util.UUID;

public class IngenieroRegistroResponse {

    private final UUID id;
    private final EstadoVerificacion estadoVerificacion;

    public IngenieroRegistroResponse(UUID id, EstadoVerificacion estadoVerificacion) {
        this.id = id;
        this.estadoVerificacion = estadoVerificacion;
    }

    public UUID getId() {
        return id;
    }

    public EstadoVerificacion getEstadoVerificacion() {
        return estadoVerificacion;
    }
}
