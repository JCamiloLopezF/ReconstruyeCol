package com.reconstruyecol.ayudaterremoto.model.dto;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class OfertaCrearRequest {

    @NotNull(message = "El tipo de ayuda es obligatorio")
    private TipoAyuda tipoAyuda;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 500, message = "La descripcion no puede superar 500 caracteres")
    private String descripcion;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double lat;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double lng;

    @Pattern(regexp = "\\d{7,15}", message = "El WhatsApp debe contener solo números (7 a 15 dígitos)")
    private String contactoWhatsapp;

    @Email(message = "El correo no es válido. Verifica el formato nombre@dominio.com")
    private String contactoEmail;

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

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
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
}