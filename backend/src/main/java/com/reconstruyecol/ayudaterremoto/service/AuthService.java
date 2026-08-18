package com.reconstruyecol.ayudaterremoto.service;

import com.reconstruyecol.ayudaterremoto.model.Ingeniero;
import com.reconstruyecol.ayudaterremoto.model.dto.LoginRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.LoginResponse;
import com.reconstruyecol.ayudaterremoto.repository.IngenieroRepository;
import com.reconstruyecol.ayudaterremoto.security.CredencialesInvalidasException;
import com.reconstruyecol.ayudaterremoto.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Correo o contraseña incorrectos";

    private final IngenieroRepository ingenieroRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(IngenieroRepository ingenieroRepository, PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.ingenieroRepository = ingenieroRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Por ahora solo autentica ingenieros (no hay registro/seed de administradores todavia).
     * El mensaje de error es igual de generico si el correo no existe o si la contraseña no
     * coincide, para no revelar cuales correos estan registrados.
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Ingeniero ingeniero = ingenieroRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS));

        if (!passwordEncoder.matches(request.getPassword(), ingeniero.getPasswordHash())) {
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
        }

        String token = jwtService.generarToken(ingeniero.getId(), ingeniero.getEmail(), "INGENIERO");
        return new LoginResponse(token, "INGENIERO", ingeniero.getEstadoVerificacion());
    }
}
