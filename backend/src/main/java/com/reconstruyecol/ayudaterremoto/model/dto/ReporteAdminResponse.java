package com.reconstruyecol.ayudaterremoto.model.dto;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.common.TipoEntidadReportada;

import java.time.Instant;
import java.util.UUID;

public class ReporteAdminResponse {

    private final UUID id;
    private final UUID entidadId;
    private final TipoEntidadReportada tipoEntidad;
    private final String motivo;
    private final Instant createdAt;
    /** Null si la publicación ya fue eliminada (por ejemplo, por otro reporte anterior). */
    private final String descripcionPublicacion;
    private final TipoAyuda tipoAyudaPublicacion;

    public ReporteAdminResponse(UUID id, UUID entidadId, TipoEntidadReportada tipoEntidad, String motivo,
                                 Instant createdAt, String descripcionPublicacion, TipoAyuda tipoAyudaPublicacion) {
        this.id = id;
        this.entidadId = entidadId;
        this.tipoEntidad = tipoEntidad;
        this.motivo = motivo;
        this.createdAt = createdAt;
        this.descripcionPublicacion = descripcionPublicacion;
        this.tipoAyudaPublicacion = tipoAyudaPublicacion;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEntidadId() {
        return entidadId;
    }

    public TipoEntidadReportada getTipoEntidad() {
        return tipoEntidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getDescripcionPublicacion() {
        return descripcionPublicacion;
    }

    public TipoAyuda getTipoAyudaPublicacion() {
        return tipoAyudaPublicacion;
    }
}
