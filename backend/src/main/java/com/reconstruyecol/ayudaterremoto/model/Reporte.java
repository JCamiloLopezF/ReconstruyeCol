package com.reconstruyecol.ayudaterremoto.model;

import com.reconstruyecol.ayudaterremoto.common.TipoEntidadReportada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entidad_id", nullable = false)
    private UUID entidadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entidad", nullable = false, length = 20)
    private TipoEntidadReportada tipoEntidad;

    @Column(nullable = false, columnDefinition = "text")
    private String motivo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Reporte() {
    }

    public Reporte(UUID entidadId, TipoEntidadReportada tipoEntidad, String motivo) {
        this.entidadId = entidadId;
        this.tipoEntidad = tipoEntidad;
        this.motivo = motivo;
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
}
