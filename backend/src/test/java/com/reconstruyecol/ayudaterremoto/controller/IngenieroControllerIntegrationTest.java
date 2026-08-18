package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.common.EstadoVerificacion;
import com.reconstruyecol.ayudaterremoto.model.dto.IngenieroRegistroResponse;
import com.reconstruyecol.ayudaterremoto.storage.SoporteStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IngenieroControllerIntegrationTest {

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

    // Evita depender de credenciales reales de Supabase Storage en los tests.
    @MockitoBean
    private SoporteStorageService soporteStorageService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void configurar() {
        jdbcTemplate.execute("DELETE FROM ingenieros");
        Mockito.when(soporteStorageService.subir(Mockito.any(), Mockito.anyString()))
                .thenReturn("soportes-ingenieros/fake-path.pdf");
    }

    private String urlBase() {
        return "http://localhost:" + port + "/api/ingenieros";
    }

    @Test
    void registro_conDatosValidos_retorna201YEstadoPendiente() {
        HttpEntity<MultiValueMap<String, Object>> entity = formularioValido("ana@example.com", "1020304050");

        ResponseEntity<IngenieroRegistroResponse> response = restTemplate.postForEntity(
                urlBase() + "/registro", entity, IngenieroRegistroResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getEstadoVerificacion()).isEqualTo(EstadoVerificacion.PENDIENTE);
    }

    @Test
    void registro_conDocumentoDuplicado_retorna400() {
        restTemplate.postForEntity(urlBase() + "/registro",
                formularioValido("primero@example.com", "1020304050"), IngenieroRegistroResponse.class);

        ResponseEntity<Map> response = restTemplate.postForEntity(urlBase() + "/registro",
                formularioValido("segundo@example.com", "1020304050"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void registro_conCorreoDuplicado_retorna400() {
        restTemplate.postForEntity(urlBase() + "/registro",
                formularioValido("repetido@example.com", "1020304050"), IngenieroRegistroResponse.class);

        ResponseEntity<Map> response = restTemplate.postForEntity(urlBase() + "/registro",
                formularioValido("repetido@example.com", "1111111111"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private HttpEntity<MultiValueMap<String, Object>> formularioValido(String email, String documento) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("nombre", "Ana Torres");
        form.add("email", email);
        form.add("password", "password123");
        form.add("documentoIdentidad", documento);
        form.add("universidad", "Universidad Nacional");
        form.add("fechaGraduacion", "2020-06-15");
        form.add("soporte", new ByteArrayResource("contenido-de-prueba".getBytes()) {
            @Override
            public String getFilename() {
                return "diploma.pdf";
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return new HttpEntity<>(form, headers);
    }
}
