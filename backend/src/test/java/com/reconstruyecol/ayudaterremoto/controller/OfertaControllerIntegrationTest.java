package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaCrearResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.OfertaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OfertaControllerIntegrationTest {

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
        return "http://localhost:" + port + "/api/ofertas";
    }

    @Test
    void crearOferta_conDatosValidos_retorna201ConIdYToken() {
        OfertaCrearRequest request = new OfertaCrearRequest();
        request.setTipoAyuda(TipoAyuda.TRANSPORTE);
        request.setDescripcion("Camioneta disponible para traslados");
        request.setLat(5.6947);
        request.setLng(-76.6584);
        request.setContactoWhatsapp("573007654321");

        ResponseEntity<OfertaCrearResponse> response =
                restTemplate.postForEntity(urlBase(), request, OfertaCrearResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getTokenGestion()).isNotBlank();
    }

    @Test
    void crearOferta_sinNingunContacto_retorna400() {
        OfertaCrearRequest request = new OfertaCrearRequest();
        request.setTipoAyuda(TipoAyuda.MAQUINARIA);
        request.setDescripcion("Retroexcavadora disponible");
        request.setLat(5.6947);
        request.setLng(-76.6584);

        ResponseEntity<Map> response = restTemplate.postForEntity(urlBase(), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void buscarCercanas_filtraPorRadioYTipo() {
        double latBusqueda = 5.6947;
        double lngBusqueda = -76.6584;

        crear(TipoAyuda.MEDICAMENTOS_SALUD, "Cerca del punto de busqueda", latBusqueda + 0.0005, lngBusqueda);
        crear(TipoAyuda.MEDICAMENTOS_SALUD, "A mas de 50 km del punto de busqueda", latBusqueda + 0.5, lngBusqueda);
        crear(TipoAyuda.ROPA_ABRIGO, "Cerca pero de otro tipo de ayuda", latBusqueda + 0.0005, lngBusqueda);

        String url = urlBase() + "?lat=" + latBusqueda + "&lng=" + lngBusqueda
                + "&radio=500&tipo=MEDICAMENTOS_SALUD";
        ResponseEntity<OfertaResponse[]> response = restTemplate.getForEntity(url, OfertaResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTipoAyuda()).isEqualTo(TipoAyuda.MEDICAMENTOS_SALUD);
        assertThat(response.getBody()[0].getDescripcion()).isEqualTo("Cerca del punto de busqueda");
    }

    @Test
    void marcarAtendida_conTokenValido_cambiaEstadoYDesapareceDeLaBusqueda() {
        OfertaCrearRequest request = new OfertaCrearRequest();
        request.setTipoAyuda(TipoAyuda.TRANSPORTE);
        request.setDescripcion("Oferta para marcar atendida");
        request.setLat(6.0);
        request.setLng(-77.5);
        request.setContactoEmail("contacto@example.com");
        OfertaCrearResponse creada =
                restTemplate.postForEntity(urlBase(), request, OfertaCrearResponse.class).getBody();

        ResponseEntity<Void> respuesta = restTemplate.exchange(
                urlBase() + "/" + creada.getId() + "/atendida?token=" + creada.getTokenGestion(),
                HttpMethod.PATCH, null, Void.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        String url = urlBase() + "?lat=6.0&lng=-77.5&radio=1000&tipo=TRANSPORTE";
        ResponseEntity<OfertaResponse[]> busqueda = restTemplate.getForEntity(url, OfertaResponse[].class);
        assertThat(busqueda.getBody()).isEmpty();
    }

    @Test
    void marcarAtendida_conTokenInvalido_retorna400() {
        OfertaCrearRequest request = new OfertaCrearRequest();
        request.setTipoAyuda(TipoAyuda.TRANSPORTE);
        request.setDescripcion("Oferta con token incorrecto");
        request.setLat(6.1);
        request.setLng(-77.6);
        request.setContactoEmail("contacto@example.com");
        OfertaCrearResponse creada =
                restTemplate.postForEntity(urlBase(), request, OfertaCrearResponse.class).getBody();

        ResponseEntity<Map> respuesta = restTemplate.exchange(
                urlBase() + "/" + creada.getId() + "/atendida?token=token-incorrecto",
                HttpMethod.PATCH, null, Map.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private void crear(TipoAyuda tipo, String descripcion, double lat, double lng) {
        OfertaCrearRequest request = new OfertaCrearRequest();
        request.setTipoAyuda(tipo);
        request.setDescripcion(descripcion);
        request.setLat(lat);
        request.setLng(lng);
        request.setContactoEmail("contacto@example.com");
        restTemplate.postForEntity(urlBase(), request, OfertaCrearResponse.class);
    }
}