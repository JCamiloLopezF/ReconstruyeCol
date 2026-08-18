package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.common.HashUtils;
import com.reconstruyecol.ayudaterremoto.model.Ingeniero;
import com.reconstruyecol.ayudaterremoto.model.dto.LoginRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.LoginResponse;
import com.reconstruyecol.ayudaterremoto.repository.IngenieroRepository;
import com.reconstruyecol.ayudaterremoto.storage.SoporteStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("ayudaterremoto_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @MockitoBean
    private SoporteStorageService soporteStorageService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IngenieroRepository ingenieroRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limpiarBase() {
        jdbcTemplate.execute("DELETE FROM ingenieros");
    }

    private String urlBase() {
        return "http://localhost:" + port + "/api/auth";
    }

    private void crearIngeniero(String email, String password, String documento) {
        Ingeniero ingeniero = new Ingeniero(
                "Ana Torres", email, passwordEncoder.encode(password), HashUtils.sha256(documento),
                "Universidad Nacional", LocalDate.of(2020, 6, 15), "soportes-ingenieros/fake.pdf");
        ingenieroRepository.save(ingeniero);
    }

    @Test
    void login_conCredencialesValidas_retornaTokenYEstado() {
        crearIngeniero("ana@example.com", "password123", "1020304050");

        LoginRequest request = new LoginRequest();
        request.setEmail("ana@example.com");
        request.setPassword("password123");

        ResponseEntity<LoginResponse> response =
                restTemplate.postForEntity(urlBase() + "/login", request, LoginResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getRol()).isEqualTo("INGENIERO");
    }

    @Test
    void login_conPasswordIncorrecta_retorna401() {
        crearIngeniero("ana2@example.com", "password123", "1020304051");

        LoginRequest request = new LoginRequest();
        request.setEmail("ana2@example.com");
        request.setPassword("password-equivocada");

        ResponseEntity<Map> response = restTemplate.postForEntity(urlBase() + "/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_conCorreoInexistente_retorna401() {
        LoginRequest request = new LoginRequest();
        request.setEmail("no-existe@example.com");
        request.setPassword("password123");

        ResponseEntity<Map> response = restTemplate.postForEntity(urlBase() + "/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_conMuchosIntentosFallidos_bloqueaTemporalmente() {
        crearIngeniero("fuerza-bruta@example.com", "password-correcta", "1020304099");

        LoginRequest request = new LoginRequest();
        request.setEmail("fuerza-bruta@example.com");
        request.setPassword("password-equivocada");

        HttpStatus ultimoEstado = null;
        for (int i = 0; i < 6; i++) {
            ResponseEntity<Map> response = restTemplate.postForEntity(urlBase() + "/login", request, Map.class);
            ultimoEstado = (HttpStatus) response.getStatusCode();
        }

        assertThat(ultimoEstado).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
