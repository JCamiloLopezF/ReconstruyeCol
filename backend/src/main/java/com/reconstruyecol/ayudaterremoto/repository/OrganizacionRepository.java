package com.reconstruyecol.ayudaterremoto.repository;

import com.reconstruyecol.ayudaterremoto.model.Organizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizacionRepository extends JpaRepository<Organizacion, UUID> {
}
