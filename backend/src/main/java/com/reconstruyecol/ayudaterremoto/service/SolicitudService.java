package com.reconstruyecol.ayudaterremoto.service;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.mapper.SolicitudMapper;
import com.reconstruyecol.ayudaterremoto.model.Solicitud;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudResponse;
import com.reconstruyecol.ayudaterremoto.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class SolicitudService {

    /** Radio y umbral del algoritmo de agrupacion/urgencia (docs/02-diseno-tecnico.md, seccion 5). */
    private static final double RADIO_AGRUPACION_METROS = 100;
    private static final int UMBRAL_URGENCIA = 3;

    private final SolicitudRepository solicitudRepository;

    public SolicitudService(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    @Transactional
    public SolicitudCrearResponse crear(SolicitudCrearRequest request) {
        validarContacto(request.getContactoWhatsapp(), request.getContactoEmail());

        // Se buscan las cercanas ANTES de guardar la nueva solicitud para no contarla dos veces.
        List<Solicitud> cercanas = solicitudRepository.buscarCercanas(
                request.getLat(), request.getLng(), RADIO_AGRUPACION_METROS, request.getTipoAyuda().name());

        Solicitud solicitud = SolicitudMapper.toEntity(request);
        solicitud = solicitudRepository.save(solicitud);
        agruparYMarcarUrgenciaSiAplica(cercanas);

        return SolicitudMapper.toCrearResponse(solicitud);
    }

    /**
     * Agrupa bajo el registro activo mas antiguo del cluster (mismo tipo, radio de 100 m) y lo
     * marca urgente cuando el total (cercanas + la nueva) llega a 3 o mas. Ninguna solicitud se
     * descarta: todas quedan guardadas individualmente, solo se actualiza el contador del ancla
     * del cluster para que el mapa muestre un unico pin agregado.
     */
    private void agruparYMarcarUrgenciaSiAplica(List<Solicitud> cercanas) {
        int total = cercanas.size() + 1;
        if (total < UMBRAL_URGENCIA) {
            return;
        }
        // total >= 3 implica que cercanas ya tiene al menos 2 registros previos, por lo que
        // siempre hay una solicitud mas antigua que la recien creada.
        Solicitud masAntigua = cercanas.stream()
                .min(Comparator.comparing(Solicitud::getCreatedAt))
                .orElseThrow();
        masAntigua.setUrgente(true);
        masAntigua.setSolicitudesAgrupadas(total);
        solicitudRepository.save(masAntigua);
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> buscarCercanas(double lat, double lng, double radioMetros, TipoAyuda tipoAyuda) {
        String tipo = tipoAyuda != null ? tipoAyuda.name() : null;
        return solicitudRepository.buscarCercanas(lat, lng, radioMetros, tipo)
                .stream()
                .map(SolicitudMapper::toResponse)
                .toList();
    }

    private static void validarContacto(String whatsapp, String email) {
        boolean sinWhatsapp = whatsapp == null || whatsapp.isBlank();
        boolean sinEmail = email == null || email.isBlank();
        if (sinWhatsapp && sinEmail) {
            throw new IllegalArgumentException(
                    "Debe indicar al menos un medio de contacto: WhatsApp o correo");
        }
    }
}