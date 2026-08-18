package com.reconstruyecol.ayudaterremoto.controller;

import com.reconstruyecol.ayudaterremoto.common.EstadoVerificacion;
import com.reconstruyecol.ayudaterremoto.common.TipoAyuda;
import com.reconstruyecol.ayudaterremoto.model.dto.IngenieroPendienteResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.LoginRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.LoginResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.ReporteAdminResponse;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearRequest;
import com.reconstruyecol.ayudaterremoto.model.dto.SolicitudCrearResponse;
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
import org.springframework.http.HttpMethod;
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
class AdminControllerIntegrationTest {

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

    private String tokenAdmin;

    @BeforeEach
    void configurar() {
        jdbcTemplate.execute("DELETE FROM reportes");
        jdbcTemplate.execute("DELETE FROM ingenieros");
        jdbcTemplate.execute("DELETE FROM solicitudes");
        jdbcTemplate.execute("DELETE FROM ofertas");

        Mockito.when(soporteStorageService.subir(Mockito.any(), Mockito.anyString()))
                .thenReturn("soportes-ingenieros/fake-path.pdf");
        Mockito.when(soporteStorageService.generarUrlFirmada(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn("https://fake.supabase.co/storage/v1/object/sign/fake.pdf?token=abc");

        // AdminSeeder ya crea un admin con las credenciales default de application.yml al arrancar
        // el contexto de test (no se sobreescriben aqui a proposito, para probar el flujo real).
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@reconstruyecol.org");
        login.setPassword("cambiar-esta-password-admin-0123456789");
        ResponseEntity<LoginResponse> respuesta =
                restTemplate.postForEntity(url("/api/auth/login"), login, LoginResponse.class);
        tokenAdmin = respuesta.getBody().getToken();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders headersConToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void endpointsAdmin_sinToken_retorna403() {
        ResponseEntity<Map> respuesta = restTemplate.getForEntity(url("/api/admin/ingenieros/pendientes"), Map.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void endpointsAdmin_conTokenDeIngeniero_retorna403() {
        registrarIngeniero("ing@example.com", "1010101010");
        LoginRequest login = new LoginRequest();
        login.setEmail("ing@example.com");
        login.setPassword("password123");
        ResponseEntity<LoginResponse> loginResp =
                restTemplate.postForEntity(url("/api/auth/login"), login, LoginResponse.class);

        HttpEntity<Void> entity = new HttpEntity<>(headersConToken(loginResp.getBody().getToken()));
        ResponseEntity<Map> respuesta = restTemplate.exchange(
                url("/api/admin/ingenieros/pendientes"), HttpMethod.GET, entity, Map.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void ingenieroPendiente_aprobarLoCambiaAVerificado() {
        registrarIngeniero("aprobar@example.com", "2020202020");

        HttpEntity<Void> entityGet = new HttpEntity<>(headersConToken(tokenAdmin));
        ResponseEntity<IngenieroPendienteResponse[]> pendientes = restTemplate.exchange(
                url("/api/admin/ingenieros/pendientes"), HttpMethod.GET, entityGet, IngenieroPendienteResponse[].class);
        assertThat(pendientes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pendientes.getBody()).hasSize(1);
        assertThat(pendientes.getBody()[0].getUrlSoporte()).startsWith("https://");

        var id = pendientes.getBody()[0].getId();
        HttpHeaders headers = headersConToken(tokenAdmin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entityPatch = new HttpEntity<>(Map.of("estado", "VERIFICADO"), headers);

        ResponseEntity<Void> respuesta = restTemplate.exchange(
                url("/api/admin/ingenieros/" + id + "/estado"), HttpMethod.PATCH, entityPatch, Void.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        String estado = jdbcTemplate.queryForObject(
                "SELECT estado_verificacion FROM ingenieros WHERE id = ?::uuid", String.class, id.toString());
        assertThat(estado).isEqualTo(EstadoVerificacion.VERIFICADO.name());
    }

    @Test
    void reportarPublicacion_apareceEnListaAdmin_yEliminarLaBorra() {
        String idSolicitud = crearSolicitud();

        HttpHeaders headersReporte = new HttpHeaders();
        headersReporte.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> reporte = Map.of(
                "entidadId", idSolicitud,
                "tipoEntidad", "SOLICITUD",
                "motivo", "Contenido falso");
        ResponseEntity<Void> respuestaReporte = restTemplate.postForEntity(
                url("/api/reportes"), new HttpEntity<>(reporte, headersReporte), Void.class);
        assertThat(respuestaReporte.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        HttpEntity<Void> entityGet = new HttpEntity<>(headersConToken(tokenAdmin));
        ResponseEntity<ReporteAdminResponse[]> reportes = restTemplate.exchange(
                url("/api/admin/reportes"), HttpMethod.GET, entityGet, ReporteAdminResponse[].class);
        assertThat(reportes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reportes.getBody()).hasSize(1);
        assertThat(reportes.getBody()[0].getDescripcionPublicacion()).isEqualTo("Necesitamos agua potable");
        assertThat(reportes.getBody()[0].getTipoAyudaPublicacion()).isEqualTo(TipoAyuda.AGUA);

        ResponseEntity<Void> respuestaEliminar = restTemplate.exchange(
                url("/api/admin/publicaciones/" + idSolicitud + "?tipo=SOLICITUD"),
                HttpMethod.DELETE, entityGet, Void.class);
        assertThat(respuestaEliminar.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Integer solicitudesRestantes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM solicitudes WHERE id = ?::uuid", Integer.class, idSolicitud);
        assertThat(solicitudesRestantes).isZero();

        ResponseEntity<ReporteAdminResponse[]> reportesDespues = restTemplate.exchange(
                url("/api/admin/reportes"), HttpMethod.GET, entityGet, ReporteAdminResponse[].class);
        assertThat(reportesDespues.getBody()).isEmpty();
    }

    private void registrarIngeniero(String email, String documento) {
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
        restTemplate.postForEntity(url("/api/ingenieros/registro"), new HttpEntity<>(form, headers), Void.class);
    }

    private String crearSolicitud() {
        SolicitudCrearRequest request = new SolicitudCrearRequest();
        request.setTipoAyuda(TipoAyuda.AGUA);
        request.setDescripcion("Necesitamos agua potable");
        request.setLat(5.6947);
        request.setLng(-76.6584);
        request.setContactoEmail("contacto@example.com");
        ResponseEntity<SolicitudCrearResponse> respuesta =
                restTemplate.postForEntity(url("/api/solicitudes"), request, SolicitudCrearResponse.class);
        return respuesta.getBody().getId().toString();
    }
}
