package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaCrearResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearResponse;
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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReporteControllerIntegrationTest {

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
        jdbcTemplate.execute("DELETE FROM reportes");
        jdbcTemplate.execute("DELETE FROM solicitudes");
        jdbcTemplate.execute("DELETE FROM ofertas");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void reportar_unaSolicitudExistente_retorna201() {
        String idSolicitud = crearSolicitud();

        Map<String, Object> reporte = Map.of(
                "entidadId", idSolicitud,
                "tipoEntidad", "SOLICITUD",
                "motivo", "Parece contenido falso");

        ResponseEntity<Void> respuesta = restTemplate.postForEntity(url("/api/reportes"), reporte, Void.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reportes", Integer.class);
        assertThat(total).isEqualTo(1);
    }

    @Test
    void reportar_unaOfertaExistente_retorna201() {
        String idOferta = crearOferta();

        Map<String, Object> reporte = Map.of(
                "entidadId", idOferta,
                "tipoEntidad", "OFERTA",
                "motivo", "No responde al contacto");

        ResponseEntity<Void> respuesta = restTemplate.postForEntity(url("/api/reportes"), reporte, Void.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void reportar_entidadInexistente_retorna400() {
        Map<String, Object> reporte = Map.of(
                "entidadId", UUID.randomUUID().toString(),
                "tipoEntidad", "SOLICITUD",
                "motivo", "Cualquier motivo");

        ResponseEntity<Map> respuesta = restTemplate.postForEntity(url("/api/reportes"), reporte, Map.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void reportar_sinMotivo_retorna400() {
        String idSolicitud = crearSolicitud();

        Map<String, Object> reporte = Map.of(
                "entidadId", idSolicitud,
                "tipoEntidad", "SOLICITUD",
                "motivo", "");

        ResponseEntity<Map> respuesta = restTemplate.postForEntity(url("/api/reportes"), reporte, Map.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private String crearSolicitud() {
        SolicitudCrearRequest request = new SolicitudCrearRequest();
        request.setTipoAyuda(TipoAyuda.AGUA);
        request.setDescripcion("Solicitud para reportar");
        request.setLat(5.6947);
        request.setLng(-76.6584);
        request.setContactoEmail("contacto@example.com");
        ResponseEntity<SolicitudCrearResponse> respuesta =
                restTemplate.postForEntity(url("/api/solicitudes"), request, SolicitudCrearResponse.class);
        return respuesta.getBody().getId().toString();
    }

    private String crearOferta() {
        OfertaCrearRequest request = new OfertaCrearRequest();
        request.setTipoAyuda(TipoAyuda.TRANSPORTE);
        request.setDescripcion("Oferta para reportar");
        request.setLat(5.6947);
        request.setLng(-76.6584);
        request.setContactoEmail("contacto@example.com");
        ResponseEntity<OfertaCrearResponse> respuesta =
                restTemplate.postForEntity(url("/api/ofertas"), request, OfertaCrearResponse.class);
        return respuesta.getBody().getId().toString();
    }
}
