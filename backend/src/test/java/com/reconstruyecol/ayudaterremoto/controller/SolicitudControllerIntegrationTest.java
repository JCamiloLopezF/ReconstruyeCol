package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudResponse;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SolicitudControllerIntegrationTest {

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

    private String urlBase() {
        return "http://localhost:" + port + "/api/solicitudes";
    }

    @Test
    void crearSolicitud_conDatosValidos_retorna201ConIdYToken() {
        SolicitudCrearRequest request = new SolicitudCrearRequest();
        request.setTipoAyuda(TipoAyuda.AGUA);
        request.setDescripcion("Necesitamos agua potable para 5 familias");
        request.setLat(5.6947);
        request.setLng(-76.6584);
        request.setContactoWhatsapp("573001234567");

        ResponseEntity<SolicitudCrearResponse> response =
                restTemplate.postForEntity(urlBase(), request, SolicitudCrearResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getTokenGestion()).isNotBlank();
    }

    @Test
    void crearSolicitud_sinNingunContacto_retorna400() {
        SolicitudCrearRequest request = new SolicitudCrearRequest();
        request.setTipoAyuda(TipoAyuda.ALIMENTO);
        request.setDescripcion("Necesitamos alimentos no perecederos");
        request.setLat(5.6947);
        request.setLng(-76.6584);

        ResponseEntity<Map> response = restTemplate.postForEntity(urlBase(), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void buscarCercanas_filtraPorRadioYTipo() {
        // Quibdo (Choco), punto de referencia de la busqueda
        double latBusqueda = 5.6947;
        double lngBusqueda = -76.6584;

        crear(TipoAyuda.AGUA, "Cerca del punto de busqueda", latBusqueda + 0.0005, lngBusqueda);
        crear(TipoAyuda.AGUA, "A mas de 50 km del punto de busqueda", latBusqueda + 0.5, lngBusqueda);
        crear(TipoAyuda.ALIMENTO, "Cerca pero de otro tipo de ayuda", latBusqueda + 0.0005, lngBusqueda);

        String url = urlBase() + "?lat=" + latBusqueda + "&lng=" + lngBusqueda
                + "&radio=500&tipo=AGUA";
        ResponseEntity<SolicitudResponse[]> response = restTemplate.getForEntity(url, SolicitudResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTipoAyuda()).isEqualTo(TipoAyuda.AGUA);
        assertThat(response.getBody()[0].getDescripcion()).isEqualTo("Cerca del punto de busqueda");
    }

    @Test
    void crearSolicitud_alLlegarCuartaCercana_marcaClusterComoUrgenteSinDescartarDatos() {
        // Punto alejado de los demas tests de esta clase para no compartir datos con ellos
        // (el contenedor de Postgres es estatico y se reusa entre metodos de test).
        double lat = 5.0000;
        double lng = -77.0000;

        crear(TipoAyuda.AGUA, "Primera solicitud del cluster", lat, lng);
        crear(TipoAyuda.AGUA, "Segunda solicitud del cluster", lat + 0.0001, lng);
        crear(TipoAyuda.AGUA, "Tercera solicitud del cluster", lat + 0.0002, lng);
        crear(TipoAyuda.AGUA, "Cuarta solicitud del cluster", lat + 0.0003, lng);

        String url = urlBase() + "?lat=" + lat + "&lng=" + lng + "&radio=1000&tipo=AGUA";
        ResponseEntity<SolicitudResponse[]> response = restTemplate.getForEntity(url, SolicitudResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SolicitudResponse[] solicitudes = response.getBody();
        assertThat(solicitudes).isNotNull();
        // Agrupar nunca descarta datos: las 4 solicitudes siguen existiendo individualmente.
        assertThat(solicitudes).hasSize(4);

        List<SolicitudResponse> urgentes = Arrays.stream(solicitudes)
                .filter(SolicitudResponse::isUrgente)
                .toList();
        assertThat(urgentes).hasSize(1);
        assertThat(urgentes.get(0).getDescripcion()).isEqualTo("Primera solicitud del cluster");
        assertThat(urgentes.get(0).getSolicitudesAgrupadas()).isEqualTo(4);

        List<SolicitudResponse> noUrgentes = Arrays.stream(solicitudes)
                .filter(s -> !s.isUrgente())
                .toList();
        assertThat(noUrgentes).hasSize(3);
        assertThat(noUrgentes).allSatisfy(s -> assertThat(s.getSolicitudesAgrupadas()).isEqualTo(1));
    }

    private void crear(TipoAyuda tipo, String descripcion, double lat, double lng) {
        SolicitudCrearRequest request = new SolicitudCrearRequest();
        request.setTipoAyuda(tipo);
        request.setDescripcion(descripcion);
        request.setLat(lat);
        request.setLng(lng);
        request.setContactoEmail("contacto@example.com");
        restTemplate.postForEntity(urlBase(), request, SolicitudCrearResponse.class);
    }
}