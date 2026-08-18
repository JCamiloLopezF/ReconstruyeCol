package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.common.TipoOrganizacion;
import com.reconstruyecol.ayudaterremoto.model.dto.OrganizacionCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.OrganizacionCrearResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearRequest;
import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
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
class OrganizacionControllerIntegrationTest {

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
        jdbcTemplate.execute("DELETE FROM organizaciones");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void crearOrganizacion_conDatosValidos_retorna201NoVerificada() {
        OrganizacionCrearRequest request = new OrganizacionCrearRequest();
        request.setNombre("Centro de Acopio La Esperanza");
        request.setTipo(TipoOrganizacion.CENTRO_ACOPIO);
        request.setLat(5.6947);
        request.setLng(-76.6584);
        request.setContacto("573001234567");

        ResponseEntity<OrganizacionCrearResponse> response = restTemplate.postForEntity(
                url("/api/organizaciones"), request, OrganizacionCrearResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getTipo()).isEqualTo(TipoOrganizacion.CENTRO_ACOPIO);
        assertThat(response.getBody().isVerificada()).isFalse();
    }

    @Test
    void crearOrganizacion_sinNombre_retorna400() {
        OrganizacionCrearRequest request = new OrganizacionCrearRequest();
        request.setTipo(TipoOrganizacion.ONG);
        request.setLat(5.6947);
        request.setLng(-76.6584);
        request.setContacto("contacto@ong.org");

        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/organizaciones"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void crearSolicitud_conOrganizacionExistente_seVinculaCorrectamente() {
        OrganizacionCrearRequest orgRequest = new OrganizacionCrearRequest();
        orgRequest.setNombre("ONG Reconstruye");
        orgRequest.setTipo(TipoOrganizacion.ONG);
        orgRequest.setLat(5.6947);
        orgRequest.setLng(-76.6584);
        orgRequest.setContacto("contacto@ong.org");
        OrganizacionCrearResponse org = restTemplate.postForEntity(
                url("/api/organizaciones"), orgRequest, OrganizacionCrearResponse.class).getBody();

        SolicitudCrearRequest solicitudRequest = new SolicitudCrearRequest();
        solicitudRequest.setTipoAyuda(TipoAyuda.AGUA);
        solicitudRequest.setDescripcion("Publicada por una organizacion");
        solicitudRequest.setLat(5.6947);
        solicitudRequest.setLng(-76.6584);
        solicitudRequest.setContactoEmail("contacto@example.com");
        solicitudRequest.setOrganizacionId(org.getId());

        ResponseEntity<SolicitudCrearResponse> response = restTemplate.postForEntity(
                url("/api/solicitudes"), solicitudRequest, SolicitudCrearResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String organizacionId = jdbcTemplate.queryForObject(
                "SELECT organizacion_id FROM solicitudes WHERE id = ?::uuid", String.class,
                response.getBody().getId().toString());
        assertThat(organizacionId).isEqualTo(org.getId().toString());
    }

    @Test
    void crearSolicitud_conOrganizacionInexistente_retorna400() {
        SolicitudCrearRequest solicitudRequest = new SolicitudCrearRequest();
        solicitudRequest.setTipoAyuda(TipoAyuda.AGUA);
        solicitudRequest.setDescripcion("Organizacion inventada");
        solicitudRequest.setLat(5.6947);
        solicitudRequest.setLng(-76.6584);
        solicitudRequest.setContactoEmail("contacto@example.com");
        solicitudRequest.setOrganizacionId(UUID.randomUUID());

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url("/api/solicitudes"), solicitudRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
