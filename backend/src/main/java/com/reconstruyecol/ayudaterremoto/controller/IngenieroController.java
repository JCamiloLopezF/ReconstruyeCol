package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.model.dto.IngenieroRegistroRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.IngenieroRegistroResponse;
import com.reconstruyecol.ayudaterremoto.service.IngenieroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingenieros")
public class IngenieroController {

    private final IngenieroService ingenieroService;

    public IngenieroController(IngenieroService ingenieroService) {
        this.ingenieroService = ingenieroService;
    }

    @PostMapping(path = "/registro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngenieroRegistroResponse> registro(@Valid @ModelAttribute IngenieroRegistroRequest request) {
        IngenieroRegistroResponse response = ingenieroService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
