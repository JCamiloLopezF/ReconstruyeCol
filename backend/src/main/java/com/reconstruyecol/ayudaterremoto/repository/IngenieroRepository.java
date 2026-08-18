package com.reconstruyecol.ayudaterremoto.repository;

import com.reconstruyecol.ayudaterremoto.common.EstadoVerificacion;
import com.reconstruyecol.ayudaterremoto.model.Ingeniero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngenieroRepository extends JpaRepository<Ingeniero, UUID> {

    Optional<Ingeniero> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDocumentoIdentidadHash(String documentoIdentidadHash);

    List<Ingeniero> findByEstadoVerificacion(EstadoVerificacion estadoVerificacion);
}
