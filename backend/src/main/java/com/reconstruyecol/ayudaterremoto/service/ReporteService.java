package com.reconstruyecol.ayudaterremoto.service;

import com.reconstruyecol.ayudaterremoto.common.TipoEntidadReportada;
import com.reconstruyecol.ayudaterremoto.model.Oferta;
import com.reconstruyecol.ayudaterremoto.model.Reporte;
import com.reconstruyecol.ayudaterremoto.model.Solicitud;
import com.reconstruyecol.ayudaterremoto.model.dto.ReporteAdminResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.ReporteCrearRequest;
import com.reconstruyecol.ayudaterremoto.repository.OfertaRepository;
import com.reconstruyecol.ayudaterremoto.repository.ReporteRepository;
import com.reconstruyecol.ayudaterremoto.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final SolicitudRepository solicitudRepository;
    private final OfertaRepository ofertaRepository;

    public ReporteService(ReporteRepository reporteRepository, SolicitudRepository solicitudRepository,
                           OfertaRepository ofertaRepository) {
        this.reporteRepository = reporteRepository;
        this.solicitudRepository = solicitudRepository;
        this.ofertaRepository = ofertaRepository;
    }

    @Transactional
    public void crear(ReporteCrearRequest request) {
        boolean existe = request.getTipoEntidad() == TipoEntidadReportada.SOLICITUD
                ? solicitudRepository.existsById(request.getEntidadId())
                : ofertaRepository.existsById(request.getEntidadId());
        if (!existe) {
            throw new IllegalArgumentException("La publicación reportada no existe");
        }
        reporteRepository.save(new Reporte(request.getEntidadId(), request.getTipoEntidad(), request.getMotivo()));
    }

    @Transactional(readOnly = true)
    public List<ReporteAdminResponse> listarPendientes() {
        return reporteRepository.findAll().stream()
                .map(this::toAdminResponse)
                .toList();
    }

    private ReporteAdminResponse toAdminResponse(Reporte reporte) {
        if (reporte.getTipoEntidad() == TipoEntidadReportada.SOLICITUD) {
            Optional<Solicitud> solicitud = solicitudRepository.findById(reporte.getEntidadId());
            return new ReporteAdminResponse(
                    reporte.getId(), reporte.getEntidadId(), reporte.getTipoEntidad(), reporte.getMotivo(),
                    reporte.getCreatedAt(),
                    solicitud.map(Solicitud::getDescripcion).orElse(null),
                    solicitud.map(Solicitud::getTipoAyuda).orElse(null));
        }
        Optional<Oferta> oferta = ofertaRepository.findById(reporte.getEntidadId());
        return new ReporteAdminResponse(
                reporte.getId(), reporte.getEntidadId(), reporte.getTipoEntidad(), reporte.getMotivo(),
                reporte.getCreatedAt(),
                oferta.map(Oferta::getDescripcion).orElse(null),
                oferta.map(Oferta::getTipoAyuda).orElse(null));
    }

    @Transactional
    public void eliminarPublicacion(UUID id, TipoEntidadReportada tipo) {
        if (tipo == TipoEntidadReportada.SOLICITUD) {
            if (!solicitudRepository.existsById(id)) {
                throw new IllegalArgumentException("Solicitud no encontrada");
            }
            solicitudRepository.deleteById(id);
        } else {
            if (!ofertaRepository.existsById(id)) {
                throw new IllegalArgumentException("Oferta no encontrada");
            }
            ofertaRepository.deleteById(id);
        }
        reporteRepository.deleteByEntidadId(id);
    }
}
