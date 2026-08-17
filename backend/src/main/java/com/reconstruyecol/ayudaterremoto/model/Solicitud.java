package com.reconstruyecol.ayudaterremoto.model;

import com.reconstruyecol.ayudaterremoto.common.EstadoPublicacion;
import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ayuda", nullable = false, length = 50)
    private TipoAyuda tipoAyuda;

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point ubicacion;

    @Column(nullable = false)
    private boolean urgente = false;

    @Column(name = "solicitudes_agrupadas", nullable = false)
    private int solicitudesAgrupadas = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPublicacion estado = EstadoPublicacion.ACTIVA;

    @Column(name = "contacto_whatsapp")
    private String contactoWhatsapp;

    @Column(name = "contacto_email")
    private String contactoEmail;

    @Column(name = "token_gestion", nullable = false, unique = true, length = 64)
    private String tokenGestion = UUID.randomUUID().toString();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Solicitud() {
    }

    public Solicitud(TipoAyuda tipoAyuda, String descripcion, Point ubicacion,
                      String contactoWhatsapp, String contactoEmail) {
        this.tipoAyuda = tipoAyuda;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.contactoWhatsapp = contactoWhatsapp;
        this.contactoEmail = contactoEmail;
    }

    public UUID getId() {
        return id;
    }

    public TipoAyuda getTipoAyuda() {
        return tipoAyuda;
    }

    public void setTipoAyuda(TipoAyuda tipoAyuda) {
        this.tipoAyuda = tipoAyuda;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Point getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Point ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isUrgente() {
        return urgente;
    }

    public void setUrgente(boolean urgente) {
        this.urgente = urgente;
    }

    public int getSolicitudesAgrupadas() {
        return solicitudesAgrupadas;
    }

    public void setSolicitudesAgrupadas(int solicitudesAgrupadas) {
        this.solicitudesAgrupadas = solicitudesAgrupadas;
    }

    public EstadoPublicacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPublicacion estado) {
        this.estado = estado;
    }

    public String getContactoWhatsapp() {
        return contactoWhatsapp;
    }

    public void setContactoWhatsapp(String contactoWhatsapp) {
        this.contactoWhatsapp = contactoWhatsapp;
    }

    public String getContactoEmail() {
        return contactoEmail;
    }

    public void setContactoEmail(String contactoEmail) {
        this.contactoEmail = contactoEmail;
    }

    public String getTokenGestion() {
        return tokenGestion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}