package com.reconstruyecol.ayudaterremoto.repository;

import com.reconstruyecol.ayudaterremoto.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReporteRepository extends JpaRepository<Reporte, UUID> {

    void deleteByEntidadId(UUID entidadId);
}
