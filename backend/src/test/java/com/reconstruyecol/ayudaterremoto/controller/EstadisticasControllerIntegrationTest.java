package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.model.dto.ConteoPorTipo;
import com.reconstruyecol.ayudaterremoto.model.dto.EstadisticasResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EstadisticasControllerIntegrationTest {

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

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limpiarBase() {
        jdbcTemplate.execute("DELETE FROM solicitudes");
        jdbcTemplate.execute("DELETE FROM ofertas");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void estadisticasPublicas_reflejanSolicitudesCreadas() {
        crearSolicitud(TipoAyuda.AGUA);
        crearSolicitud(TipoAyuda.AGUA);
        crearSolicitud(TipoAyuda.ALIMENTO);

        ResponseEntity<EstadisticasResponse> respuesta =
                restTemplate.getForEntity(url("/api/estadisticas/publicas"), EstadisticasResponse.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        var solicitudes = respuesta.getBody().getSolicitudes();

        ConteoPorTipo conteoAgua = solicitudes.stream()
                .filter(c -> c.getTipoAyuda() == TipoAyuda.AGUA)
                .findFirst()
                .orElseThrow();
        assertThat(conteoAgua.getActivas()).isEqualTo(2);
        assertThat(conteoAgua.getAtendidas()).isEqualTo(0);

        ConteoPorTipo conteoAlimento = solicitudes.stream()
                .filter(c -> c.getTipoAyuda() == TipoAyuda.ALIMENTO)
                .findFirst()
                .orElseThrow();
        assertThat(conteoAlimento.getActivas()).isEqualTo(1);
    }

    private void crearSolicitud(TipoAyuda tipo) {
        SolicitudCrearRequest request = new SolicitudCrearRequest();
        request.setTipoAyuda(tipo);
        request.setDescripcion("Descripcion de prueba");
        request.setLat(5.6947);
        request.setLng(-76.6584);
        request.setContactoEmail("contacto@example.com");
        restTemplate.postForEntity(url("/api/solicitudes"), request, Void.class);
    }
}
