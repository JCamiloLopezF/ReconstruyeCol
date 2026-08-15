package com.reconstruyecol.ayudaterremoto.oferta;

import com.reconstruyecol.ayudaterremoto.common.GeoUtils;
import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.oferta.dto.OfertaCrearRequest;
import com.reconstruyecol.ayudaterremoto.oferta.dto.OfertaCrearResponse;
import com.reconstruyecol.ayudaterremoto.oferta.dto.OfertaResponse;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfertaService {

    private final OfertaRepository ofertaRepository;

    public OfertaService(OfertaRepository ofertaRepository) {
        this.ofertaRepository = ofertaRepository;
    }

    @Transactional
    public OfertaCrearResponse crear(OfertaCrearRequest request) {
        validarContacto(request.getContactoWhatsapp(), request.getContactoEmail());

        Point ubicacion = GeoUtils.crearPunto(request.getLat(), request.getLng());
        Oferta oferta = new Oferta(
                request.getTipoAyuda(),
                request.getDescripcion(),
                ubicacion,
                request.getContactoWhatsapp(),
                request.getContactoEmail());

        oferta = ofertaRepository.save(oferta);
        return new OfertaCrearResponse(oferta.getId(), oferta.getTokenGestion());
    }

    @Transactional(readOnly = true)
    public List<OfertaResponse> buscarCercanas(double lat, double lng, double radioMetros, TipoAyuda tipoAyuda) {
        String tipo = tipoAyuda != null ? tipoAyuda.name() : null;
        return ofertaRepository.buscarCercanas(lat, lng, radioMetros, tipo)
                .stream()
                .map(OfertaService::toResponse)
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

    private static OfertaResponse toResponse(Oferta oferta) {
        return new OfertaResponse(
                oferta.getId(),
                oferta.getTipoAyuda(),
                oferta.getDescripcion(),
                oferta.getUbicacion().getY(),
                oferta.getUbicacion().getX(),
                oferta.getContactoWhatsapp(),
                oferta.getContactoEmail(),
                oferta.getCreatedAt());
    }
}
