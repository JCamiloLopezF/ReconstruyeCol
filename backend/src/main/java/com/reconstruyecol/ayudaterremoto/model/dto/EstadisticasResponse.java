package com.reconstruyecol.ayudaterremoto.model.dto;

import java.util.List;

public class EstadisticasResponse {

    private final List<ConteoPorTipo> solicitudes;
    private final List<ConteoPorTipo> ofertas;

    public EstadisticasResponse(List<ConteoPorTipo> solicitudes, List<ConteoPorTipo> ofertas) {
        this.solicitudes = solicitudes;
        this.ofertas = ofertas;
    }

    public List<ConteoPorTipo> getSolicitudes() {
        return solicitudes;
    }

    public List<ConteoPorTipo> getOfertas() {
        return ofertas;
    }
}
