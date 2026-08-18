package com.reconstruyecol.ayudaterremoto.repository;

/** Proyeccion compartida por SolicitudRepository y OfertaRepository para las estadisticas publicas. */
public interface ConteoTipoProjection {

    String getTipoAyuda();

    long getActivas();

    long getAtendidas();
}
