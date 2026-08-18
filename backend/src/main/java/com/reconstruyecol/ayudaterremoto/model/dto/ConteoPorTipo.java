package com.reconstruyecol.ayudaterremoto.model.dto;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;

public class ConteoPorTipo {

    private final TipoAyuda tipoAyuda;
    private final long activas;
    private final long atendidas;

    public ConteoPorTipo(TipoAyuda tipoAyuda, long activas, long atendidas) {
        this.tipoAyuda = tipoAyuda;
        this.activas = activas;
        this.atendidas = atendidas;
    }

    public TipoAyuda getTipoAyuda() {
        return tipoAyuda;
    }

    public long getActivas() {
        return activas;
    }

    public long getAtendidas() {
        return atendidas;
    }
}
