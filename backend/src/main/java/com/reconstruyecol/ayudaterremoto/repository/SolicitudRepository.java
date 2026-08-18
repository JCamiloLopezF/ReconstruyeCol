package com.reconstruyecol.ayudaterremoto.repository;

import com.reconstruyecol.ayudaterremoto.model.Solicitud;
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

    @Query(value = """
            SELECT tipo_ayuda AS tipoAyuda,
                   COUNT(*) FILTER (WHERE estado = 'ACTIVA')   AS activas,
                   COUNT(*) FILTER (WHERE estado = 'ATENDIDA') AS atendidas
            FROM solicitudes
            GROUP BY tipo_ayuda
            """, nativeQuery = true)
    List<ConteoTipoProjection> contarPorTipo();
}