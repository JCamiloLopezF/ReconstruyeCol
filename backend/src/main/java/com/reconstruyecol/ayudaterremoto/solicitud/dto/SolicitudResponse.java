package com.reconstruyecol.ayudaterremoto.solicitud.dto;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;

import java.time.Instant;
import java.util.UUID;

public class SolicitudResponse {

    private final UUID id;
    private final TipoAyuda tipoAyuda;
    private final String descripcion;
    private final double lat;
    private final double lng;
    private final boolean urgente;
    private final int solicitudesAgrupadas;
    private final String contactoWhatsapp;
    private final String contactoEmail;
    private final Instant createdAt;

    public SolicitudResponse(UUID id, TipoAyuda tipoAyuda, String descripcion, double lat, double lng,
                              boolean urgente, int solicitudesAgrupadas, String contactoWhatsapp,
                              String contactoEmail, Instant createdAt) {
        this.id = id;
        this.tipoAyuda = tipoAyuda;
        this.descripcion = descripcion;
        this.lat = lat;
        this.lng = lng;
        this.urgente = urgente;
        this.solicitudesAgrupadas = solicitudesAgrupadas;
        this.contactoWhatsapp = contactoWhatsapp;
        this.contactoEmail = contactoEmail;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public TipoAyuda getTipoAyuda() {
        return tipoAyuda;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public boolean isUrgente() {
        return urgente;
    }

    public int getSolicitudesAgrupadas() {
        return solicitudesAgrupadas;
    }

    public String getContactoWhatsapp() {
        return contactoWhatsapp;
    }

    public String getContactoEmail() {
        return contactoEmail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
