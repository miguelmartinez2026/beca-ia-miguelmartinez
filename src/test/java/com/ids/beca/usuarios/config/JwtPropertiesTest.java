package com.ids.beca.usuarios.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.Test;

class JwtPropertiesTest {

	private static final String EMISOR = "ms-usuarios";
	private static final long EXPIRACION = 3600;
	private static final int LONGITUD_VALIDA = 32;

	@Test
	void secretoBase64ValidoDeAlMenos32BytesSeDecodifica() {
		byte[] esperado = new byte[LONGITUD_VALIDA];
		Arrays.fill(esperado, (byte) 1);
		JwtProperties propiedades = propiedades(Base64.getEncoder().encodeToString(esperado));

		assertThat(propiedades.claveDecodificada()).isEqualTo(esperado);
	}

	@Test
	void secretoAusenteOEnBlancoFalla() {
		JwtProperties ausente = propiedades(null);
		JwtProperties enBlanco = propiedades("   ");

		assertThatThrownBy(ausente::claveDecodificada).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(enBlanco::claveDecodificada).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void secretoQueNoEsBase64FallaSinRevelarElValorNiLaCausa() {
		String invalido = "esto-no_es*base64!";
		JwtProperties propiedades = propiedades(invalido);

		assertThatThrownBy(propiedades::claveDecodificada)
				.isInstanceOf(IllegalStateException.class)
				.hasNoCause()
				.message().doesNotContain(invalido);
	}

	@Test
	void secretoBase64UrlSafeSeRechaza() {
		JwtProperties propiedades = propiedades("-_-_".repeat(LONGITUD_VALIDA));

		assertThatThrownBy(propiedades::claveDecodificada).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void secretoDeMenosDe32BytesDecodificadosFallaSinRevelarElValor() {
		String corto = Base64.getEncoder().encodeToString(new byte[LONGITUD_VALIDA - 1]);
		JwtProperties propiedades = propiedades(corto);

		assertThatThrownBy(propiedades::claveDecodificada)
				.isInstanceOf(IllegalStateException.class)
				.message().contains("32").doesNotContain(corto);
	}

	@Test
	void toStringNoExponeElSecreto() {
		String secreto = Base64.getEncoder().encodeToString(new byte[LONGITUD_VALIDA]);

		assertThat(propiedades(secreto).toString()).doesNotContain(secreto).contains(EMISOR);
	}

	private static JwtProperties propiedades(String secreto) {
		return new JwtProperties(secreto, EMISOR, EXPIRACION);
	}

}
