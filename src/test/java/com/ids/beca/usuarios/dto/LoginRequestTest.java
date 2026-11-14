package com.ids.beca.usuarios.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class LoginRequestTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void normalizaCorreoYNoIncluyeLaContrasenaEnToString() {
		String password = "Clave-De-Prueba-1";
		LoginRequest request = new LoginRequest("  Ana@Example.COM ", password);

		assertThat(request.email()).isEqualTo("ana@example.com");
		assertThat(request.toString()).doesNotContain(password);
	}

        @Test
        void aceptaHasta72BytesUtf8YRechazaMasDe72() {
                LoginRequest limite = new LoginRequest(
                        "ana@example.com", "a".repeat(72));

                LoginRequest sobreLimite = new LoginRequest(
                        "ana@example.com", "a".repeat(73));

                // Cada letra ñ ocupa 2 bytes en UTF-8.
                LoginRequest limiteUtf8 = new LoginRequest(
                        "ana@example.com", "\u00F1".repeat(36));

                LoginRequest sobreLimiteUtf8 = new LoginRequest(
                        "ana@example.com", "\u00F1".repeat(37));

                assertThat(validator.validate(limite)).isEmpty();
                assertThat(validator.validate(limiteUtf8)).isEmpty();

                assertThat(validator.validate(sobreLimite))
                        .extracting(violation ->
                                violation.getPropertyPath().toString())
                        .contains("password");

                assertThat(validator.validate(sobreLimiteUtf8))
                        .extracting(violation ->
                                violation.getPropertyPath().toString())
                        .contains("passwordDentroDeLimiteBcrypt");
        }


	@Test
	void rechazaCorreoYContrasenaEnBlanco() {
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(new LoginRequest(" ", " "));

		assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
				.contains("email", "password");
	}

}