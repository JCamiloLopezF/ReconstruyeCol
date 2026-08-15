package com.reconstruyecol.ayudaterremoto.solicitud;

import com.reconstruyecol.ayudaterremoto.common.GeoUtils;
import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.solicitud.dto.SolicitudCrearRequest;
import com.reconstruyecol.ayudaterremoto.solicitud.dto.SolicitudCrearResponse;
import com.reconstruyecol.ayudaterremoto.solicitud.dto.SolicitudResponse;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;

    public SolicitudService(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    @Transactional
    public SolicitudCrearResponse crear(SolicitudCrearRequest request) {
        validarContacto(request.getContactoWhatsapp(), request.getContactoEmail());

        Point ubicacion = GeoUtils.crearPunto(request.getLat(), request.getLng());
        Solicitud solicitud = new Solicitud(
                request.getTipoAyuda(),
                request.getDescripcion(),
                ubicacion,
                request.getContactoWhatsapp(),
                request.getContactoEmail());

        solicitud = solicitudRepository.save(solicitud);
        return new SolicitudCrearResponse(solicitud.getId(), solicitud.getTokenGestion());
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> buscarCercanas(double lat, double lng, double radioMetros, TipoAyuda tipoAyuda) {
        String tipo = tipoAyuda != null ? tipoAyuda.name() : null;
        return solicitudRepository.buscarCercanas(lat, lng, radioMetros, tipo)
                .stream()
                .map(SolicitudService::toResponse)
                .toList();
    }

    private static void validarContacto(String whatsapp, String email) {
        boolean sinWhatsapp = whatsapp == null || whatsapp.isBlank();
        boolean sinEmail = email == null || email.isBlank();
        if (sinWhatsapp && sinEmail) {
            throw new IllegalArgumentException(
                    "Debe indicar al menos un medio de contacto: WhatsApp o correo");
        }
    }

    private static SolicitudResponse toResponse(Solicitud solicitud) {
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getTipoAyuda(),
                solicitud.getDescripcion(),
                solicitud.getUbicacion().getY(),
                solicitud.getUbicacion().getX(),
                solicitud.isUrgente(),
                solicitud.getSolicitudesAgrupadas(),
                solicitud.getContactoWhatsapp(),
                solicitud.getContactoEmail(),
                solicitud.getCreatedAt());
    }
}
