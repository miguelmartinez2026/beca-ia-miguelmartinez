package com.ids.beca.usuarios.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Produce respuestas 401 y 403 genéricas con formato ProblemDetail: no incluyen la ruta, el motivo del
 * rechazo ni detalles del token. El cuerpo se arma solo con constantes.
 */
public class ManejadorErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

	private static final String ESQUEMA_BEARER = "Bearer";
	private static final String DETALLE_NO_AUTENTICADO = "Se requiere autenticación válida.";
	private static final String DETALLE_PROHIBIDO = "No tiene permisos para realizar esta operación.";
	private static final String PLANTILLA_PROBLEMA =
			"{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\"}";

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		response.setHeader(HttpHeaders.WWW_AUTHENTICATE, ESQUEMA_BEARER);
		escribir(response, HttpStatus.UNAUTHORIZED, DETALLE_NO_AUTENTICADO);
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		escribir(response, HttpStatus.FORBIDDEN, DETALLE_PROHIBIDO);
	}

	private static void escribir(HttpServletResponse response, HttpStatus estado, String detalle)
			throws IOException {
		String cuerpo = PLANTILLA_PROBLEMA.formatted(estado.getReasonPhrase(), estado.value(), detalle);
		response.setStatus(estado.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.getOutputStream().write(cuerpo.getBytes(StandardCharsets.UTF_8));
	}

}
