package com.reconstruyecol.ayudaterremoto.repository;

import com.reconstruyecol.ayudaterremoto.model.Ingeniero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IngenieroRepository extends JpaRepository<Ingeniero, UUID> {

    Optional<Ingeniero> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDocumentoIdentidadHash(String documentoIdentidadHash);
}
