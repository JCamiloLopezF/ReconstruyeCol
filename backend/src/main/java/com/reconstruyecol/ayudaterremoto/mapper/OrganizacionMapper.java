package com.reconstruyecol.ayudaterremoto.mapper;

import com.reconstruyecol.ayudaterremoto.common.GeoUtils;
import com.reconstruyecol.ayudaterremoto.model.Organizacion;
import com.reconstruyecol.ayudaterremoto.model.dto.OrganizacionCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.OrganizacionCrearResponse;
import org.locationtech.jts.geom.Point;

public final class OrganizacionMapper {

    private OrganizacionMapper() {
    }

    public static Organizacion toEntity(OrganizacionCrearRequest request) {
        Point ubicacion = GeoUtils.crearPunto(request.getLat(), request.getLng());
        return new Organizacion(request.getNombre(), request.getTipo(), ubicacion, request.getContacto());
    }

    public static OrganizacionCrearResponse toCrearResponse(Organizacion organizacion) {
        return new OrganizacionCrearResponse(
                organizacion.getId(), organizacion.getTipo(), organizacion.isVerificada());
    }
}
