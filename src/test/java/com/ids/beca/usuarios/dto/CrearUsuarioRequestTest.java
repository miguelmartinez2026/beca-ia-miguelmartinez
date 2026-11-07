package com.ids.beca.usuarios.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;

class CrearUsuarioRequestTest {

	private static final String PROPIEDAD_LIMITE_BCRYPT = "passwordDentroDeLimiteBcrypt";

	@Test
	void contrasenaSoloEsValidaSiNoSuperaLos72BytesUtf8() {
		// Arrange
		Validator validador = Validation.buildDefaultValidatorFactory().getValidator();
		CrearUsuarioRequest con72Bytes = new CrearUsuarioRequest("Ana Prueba", "ana@example.com", "a".repeat(72));
		CrearUsuarioRequest con73Bytes = new CrearUsuarioRequest("Ana Prueba", "ana@example.com", "a".repeat(73));
		// 37 caracteres, pero cada "ñ" ocupa 2 bytes en UTF-8: 74 bytes
		CrearUsuarioRequest con74BytesPorEnes = new CrearUsuarioRequest("Ana Prueba", "ana@example.com", "ñ".repeat(37));

		// Act
		Set<ConstraintViolation<CrearUsuarioRequest>> violaciones72 = validador.validate(con72Bytes);
		Set<ConstraintViolation<CrearUsuarioRequest>> violaciones73 = validador.validate(con73Bytes);
		Set<ConstraintViolation<CrearUsuarioRequest>> violaciones74 = validador.validate(con74BytesPorEnes);

		// Assert
		assertThat(violaciones72).isEmpty();
		assertThat(violaciones73)
				.singleElement()
				.satisfies(violacion -> {
					assertThat(violacion.getPropertyPath().toString()).isEqualTo(PROPIEDAD_LIMITE_BCRYPT);
					assertThat(violacion.getConstraintDescriptor().getAnnotation()).isInstanceOf(AssertTrue.class);
				});
		assertThat(violaciones74)
				.singleElement()
				.satisfies(violacion -> {
					assertThat(violacion.getPropertyPath().toString()).isEqualTo(PROPIEDAD_LIMITE_BCRYPT);
					assertThat(violacion.getConstraintDescriptor().getAnnotation()).isInstanceOf(AssertTrue.class);
				});
	}

}
