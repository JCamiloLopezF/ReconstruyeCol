package com.reconstruyecol.ayudaterremoto.model.dto;

import com.reconstruyecol.ayudaterremoto.common.TipoOrganizacion;

import java.util.UUID;

public class OrganizacionCrearResponse {

    private final UUID id;
    private final TipoOrganizacion tipo;
    private final boolean verificada;

    public OrganizacionCrearResponse(UUID id, TipoOrganizacion tipo, boolean verificada) {
        this.id = id;
        this.tipo = tipo;
        this.verificada = verificada;
    }

    public UUID getId() {
        return id;
    }

    public TipoOrganizacion getTipo() {
        return tipo;
    }

    public boolean isVerificada() {
        return verificada;
    }
}
