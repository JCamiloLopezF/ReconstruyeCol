package com.reconstruyecol.ayudaterremoto.service;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.mapper.OfertaMapper;
import com.reconstruyecol.ayudaterremoto.model.Oferta;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaCrearResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaResponse;
import com.reconstruyecol.ayudaterremoto.repository.OfertaRepository;
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

        Oferta oferta = OfertaMapper.toEntity(request);
        oferta = ofertaRepository.save(oferta);
        return OfertaMapper.toCrearResponse(oferta);
    }

    @Transactional(readOnly = true)
    public List<OfertaResponse> buscarCercanas(double lat, double lng, double radioMetros, TipoAyuda tipoAyuda) {
        String tipo = tipoAyuda != null ? tipoAyuda.name() : null;
        return ofertaRepository.buscarCercanas(lat, lng, radioMetros, tipo)
                .stream()
                .map(OfertaMapper::toResponse)
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