package com.reconstruyecol.ayudaterremoto.model.dto;

import java.util.UUID;

public class OfertaCrearResponse {

    private final UUID id;
    private final String tokenGestion;

    public OfertaCrearResponse(UUID id, String tokenGestion) {
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