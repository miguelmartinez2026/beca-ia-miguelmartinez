package com.ids.beca.usuarios.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;

import org.junit.jupiter.api.Test;

class ClaveJwtPruebasTest {

	@Test
	void laClaveDecodificadaTieneAlMenos32Bytes() {
		byte[] decodificada = Base64.getDecoder().decode(ClaveJwtPruebas.base64());

		assertThat(decodificada).hasSizeGreaterThanOrEqualTo(32);
		assertThat(decodificada).isEqualTo(ClaveJwtPruebas.bytes());
	}

	@Test
	void laClaveEsEstableDentroDeLaJvmYNoEsConstante() {
		assertThat(ClaveJwtPruebas.base64()).isEqualTo(ClaveJwtPruebas.base64());
		assertThat(ClaveJwtPruebas.bytes()).isNotEqualTo(new byte[ClaveJwtPruebas.LONGITUD_BYTES]);
	}

}
