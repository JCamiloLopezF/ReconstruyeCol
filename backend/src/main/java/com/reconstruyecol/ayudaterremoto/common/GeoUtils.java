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
}
