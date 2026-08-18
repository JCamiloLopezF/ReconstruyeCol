package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.model.dto.OrganizacionCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.OrganizacionCrearResponse;
import com.reconstruyecol.ayudaterremoto.service.OrganizacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizaciones")
public class OrganizacionController {

    private final OrganizacionService organizacionService;

    public OrganizacionController(OrganizacionService organizacionService) {
        this.organizacionService = organizacionService;
    }

    @PostMapping
    public ResponseEntity<OrganizacionCrearResponse> crear(@Valid @RequestBody OrganizacionCrearRequest request) {
        OrganizacionCrearResponse response = organizacionService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
