
package com.ids.beca.usuarios.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class CatalogoClientTest {

    private static final String URL_CATALOGO =
            "http://localhost:8081";

    private static final String URL_CATEGORIAS =
            URL_CATALOGO + "/api/categorias";

    private RestClient restClient;
    private CatalogoProperties propiedades;
    private CatalogoClient cliente;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create();
        propiedades = new CatalogoProperties();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void establecerContextoAutenticado(String tokenValue) {
        Instant ahora = Instant.now();

        Jwt jwt = new Jwt(
                tokenValue,
                ahora,
                ahora.plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", "1",
                        "roles", List.of("SOLICITANTE")));

        JwtAuthenticationToken autenticacion =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(new SimpleGrantedAuthority(
                                "ROLE_SOLICITANTE")));

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(context);
    }

    private MockRestServiceServer prepararServidorSimulado() {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer servidor =
                MockRestServiceServer.bindTo(builder).build();

        cliente = new CatalogoClient(builder.build(), propiedades);

        return servidor;
    }

    @Test
    void construirClienteConUrlVaciaNoLanzaExcepcion() {
        propiedades.setUrl("");

        CatalogoClient resultado =
                new CatalogoClient(restClient, propiedades);

        assertThat(resultado).isNotNull();
    }

    @Test
    void construirClienteConUrlNulaNoLanzaExcepcion() {
        propiedades.setUrl(null);

        CatalogoClient resultado =
                new CatalogoClient(restClient, propiedades);

        assertThat(resultado).isNotNull();
    }

    @Test
    void consultarCategoriasConUrlVaciaLanzaCatalogoClientException() {
        propiedades.setUrl("");
        cliente = new CatalogoClient(restClient, propiedades);
        establecerContextoAutenticado("token-test");

        assertThatThrownBy(() -> cliente.consultarCategorias())
                .isInstanceOf(CatalogoClientException.class)
                .hasMessageContaining("SERVICIOS_CATALOGO_URL");
    }

    @Test
    void consultarCategoriasConUrlNulaLanzaCatalogoClientException() {
        propiedades.setUrl(null);
        cliente = new CatalogoClient(restClient, propiedades);
        establecerContextoAutenticado("token-test");

        assertThatThrownBy(() -> cliente.consultarCategorias())
                .isInstanceOf(CatalogoClientException.class)
                .hasMessageContaining("SERVICIOS_CATALOGO_URL");
    }

    @Test
    void consultarCategoriasSinAutenticacionLanzaExcepcion() {
        propiedades.setUrl(URL_CATALOGO);
        cliente = new CatalogoClient(restClient, propiedades);

        assertThatThrownBy(() -> cliente.consultarCategorias())
                .isInstanceOf(CatalogoClientException.class)
                .hasMessageContaining("no autenticado");
    }

    @Test
    void consultarCategorias200PropagaJwtYDevuelveRespuesta() {
        propiedades.setUrl(URL_CATALOGO);
        establecerContextoAutenticado("token-test");

        MockRestServiceServer servidor = prepararServidorSimulado();

        servidor.expect(requestTo(URL_CATEGORIAS))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        HttpHeaders.AUTHORIZATION, "Bearer token-test"))
                .andRespond(withSuccess(
                        "[{\"id\":1}]",
                        MediaType.APPLICATION_JSON));

        Object respuesta = cliente.consultarCategorias();

        assertThat(respuesta).isInstanceOf(List.class);
        servidor.verify();
    }

    @Test
    void consultarCategorias404LanzaNotFoundException() {
        propiedades.setUrl(URL_CATALOGO);
        establecerContextoAutenticado("token-test");

        MockRestServiceServer servidor = prepararServidorSimulado();

        servidor.expect(requestTo(URL_CATEGORIAS))
                .andRespond(withResourceNotFound());

        assertThatThrownBy(() -> cliente.consultarCategorias())
                .isInstanceOf(CatalogoClientNotFoundException.class);

        servidor.verify();
    }

    @Test
    void consultarCategorias500LanzaServiceException() {
        propiedades.setUrl(URL_CATALOGO);
        establecerContextoAutenticado("token-test");

        MockRestServiceServer servidor = prepararServidorSimulado();

        servidor.expect(requestTo(URL_CATEGORIAS))
                .andRespond(withServerError());

        assertThatThrownBy(() -> cliente.consultarCategorias())
                .isInstanceOf(CatalogoClientServiceException.class);

        servidor.verify();
    }

    @Test
    void consultarCategoriasTimeoutLanzaServiceException() {
        propiedades.setUrl(URL_CATALOGO);
        establecerContextoAutenticado("token-test");

        MockRestServiceServer servidor = prepararServidorSimulado();

        servidor.expect(requestTo(URL_CATEGORIAS))
                .andRespond(withException(
                        new SocketTimeoutException("Timeout simulado")));

        assertThatThrownBy(() -> cliente.consultarCategorias())
                .isInstanceOf(CatalogoClientServiceException.class);

        servidor.verify();
    }

    @Test
    void consultarCategorias302RechazaRedireccion() {
        propiedades.setUrl(URL_CATALOGO);
        establecerContextoAutenticado("token-test");

        MockRestServiceServer servidor = prepararServidorSimulado();

        servidor.expect(requestTo(URL_CATEGORIAS))
                .andRespond(withStatus(HttpStatus.FOUND));

        assertThatThrownBy(() -> cliente.consultarCategorias())
                .isInstanceOf(CatalogoClientServiceException.class)
                .hasMessageContaining("Redirección");

        servidor.verify();
    }

    @Test
    void consultarCategorias401LanzaExcepcionControlada() {
        propiedades.setUrl(URL_CATALOGO);
        establecerContextoAutenticado("token-test");

        MockRestServiceServer servidor = prepararServidorSimulado();

        servidor.expect(requestTo(URL_CATEGORIAS))
                .andRespond(withUnauthorizedRequest());

        assertThatThrownBy(() -> cliente.consultarCategorias())
                .isInstanceOf(CatalogoClientException.class);

        servidor.verify();
    }
}
