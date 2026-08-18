package com.reconstruyecol.ayudaterremoto.common;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public final class GeoUtils {

    public static final int SRID_WGS84 = 4326;

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    private GeoUtils() {
    }

    public static Point crearPunto(double lat, double lng) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
        point.setSRID(SRID_WGS84);
        return point;
    }

    /**
     * Redondea a 3 decimales (~110 m de margen) para no exponer la ubicación exacta en las
     * respuestas públicas (docs/02-diseno-tecnico.md, sección 8). Solo se usa al mapear hacia los
     * DTOs de salida — la geometría guardada y las búsquedas por cercanía siguen siendo exactas.
     */
    public static double aproximar(double valor) {
        return Math.round(valor * 1000.0) / 1000.0;
    }
}
