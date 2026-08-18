package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.common.TipoEntidadReportada;
import com.reconstruyecol.ayudaterremoto.model.dto.ActualizarEstadoIngenieroRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.IngenieroPendienteResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.ReporteAdminResponse;
import com.reconstruyecol.ayudaterremoto.service.IngenieroService;
import com.reconstruyecol.ayudaterremoto.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final IngenieroService ingenieroService;
    private final ReporteService reporteService;

    public AdminController(IngenieroService ingenieroService, ReporteService reporteService) {
        this.ingenieroService = ingenieroService;
        this.reporteService = reporteService;
    }

    @GetMapping("/ingenieros/pendientes")
    public List<IngenieroPendienteResponse> ingenierosPendientes() {
        return ingenieroService.listarPendientes();
    }

    @PatchMapping("/ingenieros/{id}/estado")
    public ResponseEntity<Void> actualizarEstadoIngeniero(@PathVariable UUID id,
            @Valid @RequestBody ActualizarEstadoIngenieroRequest request) {
        ingenieroService.actualizarEstado(id, request.getEstado());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reportes")
    public List<ReporteAdminResponse> reportes() {
        return reporteService.listarPendientes();
    }

    @DeleteMapping("/publicaciones/{id}")
    public ResponseEntity<Void> eliminarPublicacion(@PathVariable UUID id,
                                                      @RequestParam TipoEntidadReportada tipo) {
        reporteService.eliminarPublicacion(id, tipo);
        return ResponseEntity.noContent().build();
    }
}
