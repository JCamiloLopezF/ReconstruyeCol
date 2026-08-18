package com.reconstruyecol.ayudaterremoto.common;

import com.reconstruyecol.ayudaterremoto.model.Admin;
import com.reconstruyecol.ayudaterremoto.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Crea el primer administrador al arrancar si la tabla admins está vacía. No hay endpoint de registro. */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedEmail;
    private final String seedPassword;

    public AdminSeeder(AdminRepository adminRepository, PasswordEncoder passwordEncoder,
                        @Value("${app.admin.seed-email}") String seedEmail,
                        @Value("${app.admin.seed-password}") String seedPassword) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEmail = seedEmail;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            adminRepository.save(new Admin(seedEmail, passwordEncoder.encode(seedPassword)));
            log.warn("No había administradores: se creó uno con el correo '{}'. "
                    + "Si esto pasó en producción, cambia ADMIN_SEED_PASSWORD cuanto antes.", seedEmail);
        }
    }
}
