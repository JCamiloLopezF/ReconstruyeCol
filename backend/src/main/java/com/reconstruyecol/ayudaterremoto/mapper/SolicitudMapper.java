package com.reconstruyecol.ayudaterremoto.mapper;

import com.reconstruyecol.ayudaterremoto.common.GeoUtils;
import com.reconstruyecol.ayudaterremoto.model.Solicitud;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudResponse;
import org.locationtech.jts.geom.Point;

public final class SolicitudMapper {

    private SolicitudMapper() {
    }

    public static Solicitud toEntity(SolicitudCrearRequest request) {
        Point ubicacion = GeoUtils.crearPunto(request.getLat(), request.getLng());
        Solicitud solicitud = new Solicitud(
                request.getTipoAyuda(),
                request.getDescripcion(),
                ubicacion,
                request.getContactoWhatsapp(),
                request.getContactoEmail());
        solicitud.setOrganizacionId(request.getOrganizacionId());
        return solicitud;
    }

    public static SolicitudResponse toResponse(Solicitud solicitud) {
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getTipoAyuda(),
                solicitud.getDescripcion(),
                GeoUtils.aproximar(solicitud.getUbicacion().getY()),
                GeoUtils.aproximar(solicitud.getUbicacion().getX()),
                solicitud.isUrgente(),
                solicitud.getSolicitudesAgrupadas(),
                solicitud.getContactoWhatsapp(),
                solicitud.getContactoEmail(),
                solicitud.getCreatedAt());
    }

    public static SolicitudCrearResponse toCrearResponse(Solicitud solicitud) {
        return new SolicitudCrearResponse(solicitud.getId(), solicitud.getTokenGestion());
    }
}