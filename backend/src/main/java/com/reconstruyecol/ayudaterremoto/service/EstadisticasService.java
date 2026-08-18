package com.reconstruyecol.ayudaterremoto.service;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.model.dto.ConteoPorTipo;
import com.reconstruyecol.ayudaterremoto.model.dto.EstadisticasResponse;
import com.reconstruyecol.ayudaterremoto.repository.ConteoTipoProjection;
import com.reconstruyecol.ayudaterremoto.repository.OfertaRepository;
import com.reconstruyecol.ayudaterremoto.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadisticasService {

    private final SolicitudRepository solicitudRepository;
    private final OfertaRepository ofertaRepository;

    public EstadisticasService(SolicitudRepository solicitudRepository, OfertaRepository ofertaRepository) {
        this.solicitudRepository = solicitudRepository;
        this.ofertaRepository = ofertaRepository;
    }

    @Transactional(readOnly = true)
    public EstadisticasResponse obtenerPublicas() {
        return new EstadisticasResponse(
                solicitudRepository.contarPorTipo().stream().map(EstadisticasService::toConteo).toList(),
                ofertaRepository.contarPorTipo().stream().map(EstadisticasService::toConteo).toList());
    }

    private static ConteoPorTipo toConteo(ConteoTipoProjection proyeccion) {
        return new ConteoPorTipo(
                TipoAyuda.valueOf(proyeccion.getTipoAyuda()), proyeccion.getActivas(), proyeccion.getAtendidas());
    }
}
