package com.ids.beca.usuarios.support;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Clave JWT de pruebas: se genera una sola vez por JVM, solo existe en memoria y nunca se imprime.
 */
public final class ClaveJwtPruebas {

	public static final int LONGITUD_BYTES = 32;

	private static final byte[] CLAVE = generar();

	private ClaveJwtPruebas() {
	}

	public static byte[] bytes() {
		return CLAVE.clone();
	}

	public static String base64() {
		return Base64.getEncoder().encodeToString(CLAVE);
	}

	private static byte[] generar() {
		byte[] clave = new byte[LONGITUD_BYTES];
		new SecureRandom().nextBytes(clave);
		return clave;
	}

	/** Registra la clave como {@code seguridad.jwt.secret} en los contextos Spring completos. */
	public static class Inicializador implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext contexto) {
			TestPropertyValues.of(Map.of("seguridad.jwt.secret", base64())).applyTo(contexto);
		}

	}

}
