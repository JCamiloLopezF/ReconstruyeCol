package com.reconstruyecol.ayudaterremoto.solicitud.dto;

import java.util.UUID;

public class SolicitudCrearResponse {

    private final UUID id;
    private final String tokenGestion;

    public SolicitudCrearResponse(UUID id, String tokenGestion) {
        this.id = id;
        this.tokenGestion = tokenGestion;
    }

    public UUID getId() {
        return id;
    }

    public String getTokenGestion() {
        return tokenGestion;
    }
}
