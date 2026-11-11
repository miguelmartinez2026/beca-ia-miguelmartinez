package com.ids.beca.usuarios.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

class RolesClaimValidatorTest {

	private final RolesClaimValidator validador = new RolesClaimValidator();

	@ParameterizedTest
	@MethodSource("rolesValidos")
	void aceptaArreglosNoVaciosDeRolesConocidos(List<String> roles) {
		assertThat(validar(roles).hasErrors()).isFalse();
	}

	@ParameterizedTest
	@MethodSource("rolesInvalidos")
	void rechazaRolesAusentesVaciosMalFormadosOConocidosIncorrectos(Object roles) {
		assertThat(validar(roles).hasErrors()).isTrue();
	}

	@Test
	void rechazaTokenSinClaimRoles() {
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "HS256")
				.subject("1")
				.issuedAt(Instant.parse("2026-11-10T00:00:00Z"))
				.build();

		assertThat(validador.validate(jwt).hasErrors()).isTrue();
	}

	@Test
	void elErrorNoRevelaElContenidoDelClaim() {
		OAuth2TokenValidatorResult resultado = validar(List.of("SUPERUSUARIO"));

		assertThat(resultado.getErrors()).singleElement()
				.satisfies(error -> assertThat(error.getDescription()).doesNotContain("SUPERUSUARIO"));
	}

	static List<List<String>> rolesValidos() {
		return List.of(
				List.of("SOLICITANTE"),
				List.of("AGENTE"),
				List.of("ADMIN"),
				List.of("AGENTE", "ADMIN"));
	}

	static List<Object> rolesInvalidos() {
		return List.of(
				List.of(),
				"ADMIN",
				42,
				Map.of("rol", "ADMIN"),
				List.of("ROLE_ADMIN"),
				List.of("admin"),
				List.of("ADMIN", "DESCONOCIDO"),
				List.of("ADMIN", 1),
				List.of(List.of("ADMIN")));
	}

	private OAuth2TokenValidatorResult validar(Object roles) {
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "HS256")
				.claim(RolesClaimValidator.CLAIM_ROLES, roles)
				.build();
		return validador.validate(jwt);
	}

}
