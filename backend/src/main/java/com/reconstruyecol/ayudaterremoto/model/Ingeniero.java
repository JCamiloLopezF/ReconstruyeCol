package com.reconstruyecol.ayudaterremoto.model;

import com.reconstruyecol.ayudaterremoto.common.EstadoVerificacion;
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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ingenieros")
public class Ingeniero {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "documento_identidad_hash", nullable = false, unique = true, length = 64)
    private String documentoIdentidadHash;

    @Column(nullable = false)
    private String universidad;

    @Column(name = "fecha_graduacion", nullable = false)
    private LocalDate fechaGraduacion;

    @Column(name = "url_soporte", nullable = false, length = 500)
    private String urlSoporte;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_verificacion", nullable = false, length = 20)
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Ingeniero() {
    }

    public Ingeniero(String nombre, String email, String passwordHash, String documentoIdentidadHash,
                      String universidad, LocalDate fechaGraduacion, String urlSoporte) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.documentoIdentidadHash = documentoIdentidadHash;
        this.universidad = universidad;
        this.fechaGraduacion = fechaGraduacion;
        this.urlSoporte = urlSoporte;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDocumentoIdentidadHash() {
        return documentoIdentidadHash;
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

    public void setEstadoVerificacion(EstadoVerificacion estadoVerificacion) {
        this.estadoVerificacion = estadoVerificacion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
