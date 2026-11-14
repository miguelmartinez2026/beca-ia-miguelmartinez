package com.ids.beca.usuarios.config;

import java.util.Base64;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seguridad.jwt")
public record JwtProperties(String secret, String issuer, long expiracionSegundos) {

	static final int LONGITUD_MINIMA_BYTES = 32;

	private static final String MENSAJE_SECRETO_AUSENTE = "JWT_SECRET no está configurado.";
	private static final String MENSAJE_SECRETO_INVALIDO = "JWT_SECRET debe ser Base64 estándar válido.";
	private static final String MENSAJE_SECRETO_CORTO =
			"JWT_SECRET debe decodificar a al menos " + LONGITUD_MINIMA_BYTES + " bytes.";

	// Los mensajes nunca incluyen el valor del secreto ni la causa original del fallo de decodificación.
	public byte[] claveDecodificada() {
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException(MENSAJE_SECRETO_AUSENTE);
		}
		byte[] clave;
		try {
			clave = Base64.getDecoder().decode(secret.strip());
		} catch (IllegalArgumentException ex) {
			throw new IllegalStateException(MENSAJE_SECRETO_INVALIDO);
		}
		if (clave.length < LONGITUD_MINIMA_BYTES) {
			throw new IllegalStateException(MENSAJE_SECRETO_CORTO);
		}
		return clave;
	}

	@Override
	public String toString() {
		return "JwtProperties[secret=***, issuer=" + issuer + ", expiracionSegundos=" + expiracionSegundos
				+ "]";
	}

}
