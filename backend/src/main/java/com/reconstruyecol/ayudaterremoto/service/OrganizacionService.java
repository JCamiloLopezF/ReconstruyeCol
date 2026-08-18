package com.reconstruyecol.ayudaterremoto.service;

import com.reconstruyecol.ayudaterremoto.mapper.OrganizacionMapper;
import com.reconstruyecol.ayudaterremoto.model.Organizacion;
import com.reconstruyecol.ayudaterremoto.model.dto.OrganizacionCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.OrganizacionCrearResponse;
import com.reconstruyecol.ayudaterremoto.repository.OrganizacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizacionService {

    private final OrganizacionRepository organizacionRepository;

    public OrganizacionService(OrganizacionRepository organizacionRepository) {
        this.organizacionRepository = organizacionRepository;
    }

    @Transactional
    public OrganizacionCrearResponse crear(OrganizacionCrearRequest request) {
        Organizacion organizacion = OrganizacionMapper.toEntity(request);
        organizacion = organizacionRepository.save(organizacion);
        return OrganizacionMapper.toCrearResponse(organizacion);
    }
}
