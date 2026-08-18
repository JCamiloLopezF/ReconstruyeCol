package com.reconstruyecol.ayudaterremoto.model.dto;

import com.reconstruyecol.ayudaterremoto.common.TipoEntidadReportada;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class ReporteCrearRequest {

    @NotNull(message = "El id de la publicación es obligatorio")
    private UUID entidadId;

    @NotNull(message = "El tipo de publicación es obligatorio")
    private TipoEntidadReportada tipoEntidad;

    @NotBlank(message = "El motivo del reporte es obligatorio")
    @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
    private String motivo;

    public UUID getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(UUID entidadId) {
        this.entidadId = entidadId;
    }

    public TipoEntidadReportada getTipoEntidad() {
        return tipoEntidad;
    }

    public void setTipoEntidad(TipoEntidadReportada tipoEntidad) {
        this.tipoEntidad = tipoEntidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
