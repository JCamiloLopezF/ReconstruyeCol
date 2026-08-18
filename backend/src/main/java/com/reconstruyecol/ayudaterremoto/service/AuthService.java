package com.reconstruyecol.ayudaterremoto.service;

import com.reconstruyecol.ayudaterremoto.model.Admin;
import com.reconstruyecol.ayudaterremoto.model.Ingeniero;
import com.reconstruyecol.ayudaterremoto.model.dto.LoginRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.LoginResponse;
import com.reconstruyecol.ayudaterremoto.repository.AdminRepository;
import com.reconstruyecol.ayudaterremoto.repository.IngenieroRepository;
import com.reconstruyecol.ayudaterremoto.security.CredencialesInvalidasException;
import com.reconstruyecol.ayudaterremoto.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Correo o contraseña incorrectos";

    private final IngenieroRepository ingenieroRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(IngenieroRepository ingenieroRepository, AdminRepository adminRepository,
                        PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.ingenieroRepository = ingenieroRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Prueba primero contra ingenieros y luego contra administradores (son tablas separadas, sin
     * solape de correo esperado). El mensaje de error es igual de genérico en cualquier caso —
     * correo inexistente, password incorrecta, o cuenta en la otra tabla — para no revelar qué
     * correos existen ni de qué rol son.
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Optional<Ingeniero> ingeniero = ingenieroRepository.findByEmail(request.getEmail());
        if (ingeniero.isPresent()) {
            if (!passwordEncoder.matches(request.getPassword(), ingeniero.get().getPasswordHash())) {
                throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
            }
            String token = jwtService.generarToken(ingeniero.get().getId(), ingeniero.get().getEmail(), "INGENIERO");
            return new LoginResponse(token, "INGENIERO", ingeniero.get().getEstadoVerificacion());
        }

        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS));
        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
        }
        String token = jwtService.generarToken(admin.getId(), admin.getEmail(), "ADMIN");
        return new LoginResponse(token, "ADMIN", null);
    }
}
