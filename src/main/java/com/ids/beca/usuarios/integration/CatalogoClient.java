
package com.ids.beca.usuarios.integration;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class CatalogoClient {

    private static final int NOT_FOUND_STATUS = 404;

    private static final String RECURSO_NO_ENCONTRADO =
            "Recurso no encontrado";

    private static final String ERROR_SERVIDOR_CATALOGO =
            "Error del servidor de catálogo";

    private static final String DESTINO_INVALIDO =
            "URL de catálogo no tiene un destino válido";

    private final RestClient restClient;
    private final String baseUrl;

    public CatalogoClient(RestClient restClient,
            CatalogoProperties propiedades) {

        this.restClient = restClient.mutate().build();
        this.baseUrl = propiedades.getUrl();
    }

    public Object consultarCategorias() {
        String token = extraerToken();

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new CatalogoClientException(
                    "SERVICIOS_CATALOGO_URL no está configurada");
        }

        validarUrl(baseUrl);

        String url = baseUrl.endsWith("/")
                ? baseUrl + "api/categorias"
                : baseUrl + "/api/categorias";

        return realizarSolicitud(url, token);
    }

    private Object realizarSolicitud(String url, String token) {
        try {
            return restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is3xxRedirection,
                            (request, response) -> rechazarRedireccion())
                    .onStatus(HttpStatusCode::is4xxClientError,
                            (request, response) ->
                                    manejarErrorCliente(response.getStatusCode().value()))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, response) -> rechazarErrorServidor())
                    .body(Object.class);

        } catch (HttpClientErrorException.NotFound e) {
            throw new CatalogoClientNotFoundException(
                    RECURSO_NO_ENCONTRADO, e);

        } catch (HttpServerErrorException e) {
            throw new CatalogoClientServiceException(
                    ERROR_SERVIDOR_CATALOGO, e);

        } catch (RestClientException e) {
            throw new CatalogoClientServiceException(
                    "Error de conexión con catálogo", e);
        }
    }

    private void rechazarRedireccion() {
        throw new CatalogoClientServiceException(
                "Redirección HTTP no permitida");
    }

    private void manejarErrorCliente(int status) {
        if (status == NOT_FOUND_STATUS) {
            throw new CatalogoClientNotFoundException(
                    RECURSO_NO_ENCONTRADO);
        }

        throw new CatalogoClientException(
                "Error HTTP del cliente de catálogo");
    }

    private void rechazarErrorServidor() {
        throw new CatalogoClientServiceException(
                ERROR_SERVIDOR_CATALOGO);
    }

    private String extraerToken() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof JwtAuthenticationToken jwtAuth)
                || !jwtAuth.isAuthenticated()) {

            throw new CatalogoClientException(
                    "Usuario no autenticado o sesión inválida");
        }

        return jwtAuth.getToken().getTokenValue();
    }

    private void validarUrl(String url) {
        try {
            URI uri = new URI(url);

            validarEsquema(uri);
            validarDestino(uri);

        } catch (URISyntaxException e) {
            throw new CatalogoClientException(
                    "SERVICIOS_CATALOGO_URL es inválida", e);
        }
    }

    private void validarEsquema(URI uri) {
        String scheme = uri.getScheme();

        if (!"http".equalsIgnoreCase(scheme)
                && !"https".equalsIgnoreCase(scheme)) {

            throw new CatalogoClientException(
                    "Esquema de URL no permitido");
        }
    }

    private void validarDestino(URI uri) {
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new CatalogoClientException(DESTINO_INVALIDO);
        }

        if (uri.getRawUserInfo() != null
                || uri.getRawQuery() != null
                || uri.getRawFragment() != null) {

            throw new CatalogoClientException(DESTINO_INVALIDO);
        }
    }
}
