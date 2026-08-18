package com.reconstruyecol.ayudaterremoto.model;

import com.reconstruyecol.ayudaterremoto.common.TipoOrganizacion;
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
@Table(name = "organizaciones")
public class Organizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoOrganizacion tipo;

    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point ubicacion;

    @Column(nullable = false)
    private String contacto;

    @Column(nullable = false)
    private boolean verificada = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Organizacion() {
    }

    public Organizacion(String nombre, TipoOrganizacion tipo, Point ubicacion, String contacto) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.contacto = contacto;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoOrganizacion getTipo() {
        return tipo;
    }

    public Point getUbicacion() {
        return ubicacion;
    }

    public String getContacto() {
        return contacto;
    }

    public boolean isVerificada() {
        return verificada;
    }

    public void setVerificada(boolean verificada) {
        this.verificada = verificada;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
