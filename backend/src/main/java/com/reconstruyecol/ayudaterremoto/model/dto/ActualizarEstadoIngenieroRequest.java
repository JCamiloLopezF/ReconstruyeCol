package com.reconstruyecol.ayudaterremoto.model.dto;

import com.reconstruyecol.ayudaterremoto.common.EstadoVerificacion;
import jakarta.validation.constraints.NotNull;

public class ActualizarEstadoIngenieroRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoVerificacion estado;

    public EstadoVerificacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoVerificacion estado) {
        this.estado = estado;
    }
}
