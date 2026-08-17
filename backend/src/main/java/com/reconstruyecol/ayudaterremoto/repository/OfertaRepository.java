package com.reconstruyecol.ayudaterremoto.repository;

import com.reconstruyecol.ayudaterremoto.model.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OfertaRepository extends JpaRepository<Oferta, UUID> {

    @Query(value = """
            SELECT * FROM ofertas o
            WHERE o.estado = 'ACTIVA'
              AND (:tipoAyuda IS NULL OR o.tipo_ayuda = :tipoAyuda)
              AND ST_DWithin(
                    o.ubicacion::geography,
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                    :radioMetros
              )
            ORDER BY o.created_at DESC
            """, nativeQuery = true)
    List<Oferta> buscarCercanas(@Param("lat") double lat,
                                 @Param("lng") double lng,
                                 @Param("radioMetros") double radioMetros,
                                 @Param("tipoAyuda") String tipoAyuda);
}