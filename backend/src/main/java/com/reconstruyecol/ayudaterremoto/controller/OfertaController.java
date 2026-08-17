package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaCrearResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaResponse;
import com.reconstruyecol.ayudaterremoto.service.OfertaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ofertas")
@Validated
public class OfertaController {

    private final OfertaService ofertaService;

    public OfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    @PostMapping
    public ResponseEntity<OfertaCrearResponse> crear(@Valid @RequestBody OfertaCrearRequest request) {
        OfertaCrearResponse response = ofertaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<OfertaResponse> buscar(
            @RequestParam @NotNull(message = "La latitud es obligatoria")
            @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
            @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90") Double lat,
            @RequestParam @NotNull(message = "La longitud es obligatoria")
            @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
            @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180") Double lng,
            @RequestParam @NotNull(message = "El radio es obligatorio")
            @Positive(message = "El radio debe ser mayor a 0") Double radio,
            @RequestParam(required = false) TipoAyuda tipo) {
        return ofertaService.buscarCercanas(lat, lng, radio, tipo);
    }
}