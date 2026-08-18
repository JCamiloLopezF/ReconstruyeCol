package com.reconstruyecol.ayudaterremoto.mapper;

import com.reconstruyecol.ayudaterremoto.model.Ingeniero;
import com.reconstruyecol.ayudaterremoto.model.dto.IngenieroRegistroRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.IngenieroRegistroResponse;

public final class IngenieroMapper {

    private IngenieroMapper() {
    }

    public static Ingeniero toEntity(IngenieroRegistroRequest request, String passwordHash,
                                      String documentoIdentidadHash, String urlSoporte) {
        return new Ingeniero(
                request.getNombre(),
                request.getEmail(),
                passwordHash,
                documentoIdentidadHash,
                request.getUniversidad(),
                request.getFechaGraduacion(),
                urlSoporte);
    }

    public static IngenieroRegistroResponse toRegistroResponse(Ingeniero ingeniero) {
        return new IngenieroRegistroResponse(ingeniero.getId(), ingeniero.getEstadoVerificacion());
    }
}
