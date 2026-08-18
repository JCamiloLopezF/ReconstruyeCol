package com.reconstruyecol.ayudaterremoto.mapper;

import com.reconstruyecol.ayudaterremoto.common.GeoUtils;
import com.reconstruyecol.ayudaterremoto.model.Oferta;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaCrearResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaResponse;
import org.locationtech.jts.geom.Point;

public final class OfertaMapper {

    private OfertaMapper() {
    }

    public static Oferta toEntity(OfertaCrearRequest request) {
        Point ubicacion = GeoUtils.crearPunto(request.getLat(), request.getLng());
        return new Oferta(
                request.getTipoAyuda(),
                request.getDescripcion(),
                ubicacion,
                request.getContactoWhatsapp(),
                request.getContactoEmail());
    }

    public static OfertaResponse toResponse(Oferta oferta) {
        return new OfertaResponse(
                oferta.getId(),
                oferta.getTipoAyuda(),
                oferta.getDescripcion(),
                GeoUtils.aproximar(oferta.getUbicacion().getY()),
                GeoUtils.aproximar(oferta.getUbicacion().getX()),
                oferta.getContactoWhatsapp(),
                oferta.getContactoEmail(),
                oferta.getCreatedAt());
    }

    public static OfertaCrearResponse toCrearResponse(Oferta oferta) {
        return new OfertaCrearResponse(oferta.getId(), oferta.getTokenGestion());
    }
}