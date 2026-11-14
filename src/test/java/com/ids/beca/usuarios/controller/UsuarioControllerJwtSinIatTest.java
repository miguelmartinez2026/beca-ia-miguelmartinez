package com.ids.beca.usuarios.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ids.beca.usuarios.config.JwtConfig;
import com.ids.beca.usuarios.config.SecurityConfig;
import com.ids.beca.usuarios.service.UsuarioService;
import com.ids.beca.usuarios.support.ClaveJwtPruebas;
import com.ids.beca.usuarios.support.ConClaveJwtPruebas;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Regresión HTTP: un JWT HS256 firmado correctamente pero sin {@code iat} debe recibir 401 en un endpoint
 * protegido, usando el {@code JwtDecoder} real y sin mockearlo.
 */
@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, JwtConfig.class})
@ConClaveJwtPruebas
class UsuarioControllerJwtSinIatTest {

	private static final String EMISOR = "ms-usuarios";
	private static final Instant AHORA = Instant.parse("2026-11-10T12:00:00Z");
	// Expiración lejana y fija: la prueba no depende del reloj del sistema.
	private static final Instant EXPIRACION_LEJANA = Instant.parse("2100-01-01T00:00:00Z");
	private static final String RUTA = "/api/usuarios";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioService usuarioService;

	@Test
	void jwtFirmadoSinIatDevuelve401SinRevelarElMotivo() throws Exception {
		String token = firmarHs256(false);

		mockMvc.perform(get(RUTA).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(content().string(not(containsString("iat"))))
				.andExpect(content().string(not(containsString(token))));
		verifyNoInteractions(usuarioService);
	}

	@Test
	void mismoJwtConIatSeAceptaComoControl() throws Exception {
		when(usuarioService.listar()).thenReturn(List.of());
		String token = firmarHs256(true);

		mockMvc.perform(get(RUTA).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());
	}

	private static String firmarHs256(boolean conIat) throws JOSEException {
		JWTClaimsSet.Builder carga = new JWTClaimsSet.Builder()
				.issuer(EMISOR)
				.subject("42")
				.claim("roles", List.of("ADMIN"))
				.expirationTime(Date.from(EXPIRACION_LEJANA));
		if (conIat) {
			carga.issueTime(Date.from(AHORA));
		}
		SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), carga.build());
		jwt.sign(new MACSigner(ClaveJwtPruebas.bytes()));
		return jwt.serialize();
	}

}
