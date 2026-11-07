package com.ids.beca.usuarios.dto;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(
		@NotBlank @Size(max = 120) String nombre,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(min = 8) String password) {

	private static final int BCRYPT_MAX_BYTES = 72;

	public CrearUsuarioRequest {
		email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
	}

	// BCrypt solo procesa los primeros 72 bytes; @Size cuenta caracteres, no bytes.
	@JsonIgnore
	@AssertTrue(message = "la contraseña no debe superar 72 bytes en UTF-8")
	public boolean isPasswordDentroDeLimiteBcrypt() {
		return password == null || password.getBytes(StandardCharsets.UTF_8).length <= BCRYPT_MAX_BYTES;
	}

	@Override
	public String toString() {
		return "CrearUsuarioRequest[nombre=" + nombre + ", email=" + email + ", password=***]";
	}

}
