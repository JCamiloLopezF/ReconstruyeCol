package com.reconstruyecol.ayudaterremoto.oferta.dto;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;

import java.time.Instant;
import java.util.UUID;

public class OfertaResponse {

    private final UUID id;
    private final TipoAyuda tipoAyuda;
    private final String descripcion;
    private final double lat;
    private final double lng;
    private final String contactoWhatsapp;
    private final String contactoEmail;
    private final Instant createdAt;

    public OfertaResponse(UUID id, TipoAyuda tipoAyuda, String descripcion, double lat, double lng,
                           String contactoWhatsapp, String contactoEmail, Instant createdAt) {
        this.id = id;
        this.tipoAyuda = tipoAyuda;
        this.descripcion = descripcion;
        this.lat = lat;
        this.lng = lng;
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
