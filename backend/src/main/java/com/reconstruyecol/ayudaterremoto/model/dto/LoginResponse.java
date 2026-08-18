package com.reconstruyecol.ayudaterremoto.model.dto;

import com.reconstruyecol.ayudaterremoto.common.EstadoVerificacion;

public class LoginResponse {

    private final String token;
    private final String rol;
    private final EstadoVerificacion estadoVerificacion;

    public LoginResponse(String token, String rol, EstadoVerificacion estadoVerificacion) {
        this.token = token;
        this.rol = rol;
        this.estadoVerificacion = estadoVerificacion;
    }

    public String getToken() {
        return token;
    }

    public String getRol() {
        return rol;
    }

    public EstadoVerificacion getEstadoVerificacion() {
        return estadoVerificacion;
    }
}
