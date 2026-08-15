package com.reconstruyecol.ayudaterremoto.solicitud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SolicitudRepository extends JpaRepository<Solicitud, UUID> {

    @Query(value = """
            SELECT * FROM solicitudes s
            WHERE s.estado = 'ACTIVA'
              AND (:tipoAyuda IS NULL OR s.tipo_ayuda = :tipoAyuda)
              AND ST_DWithin(
                    s.ubicacion::geography,
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                    :radioMetros
              )
            ORDER BY s.created_at DESC
            """, nativeQuery = true)
    List<Solicitud> buscarCercanas(@Param("lat") double lat,
                                    @Param("lng") double lng,
                                    @Param("radioMetros") double radioMetros,
                                    @Param("tipoAyuda") String tipoAyuda);
}
