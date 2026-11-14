package com.ids.beca.usuarios.dto;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ids.beca.usuarios.domain.RestriccionesUsuario;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@NotBlank @Email @Size(max = RestriccionesUsuario.LONGITUD_MAXIMA_EMAIL) String email,
		@NotBlank @Size(max = RestriccionesUsuario.LONGITUD_MAXIMA_PASSWORD_BCRYPT_BYTES) String password) {

	public LoginRequest {
		email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
	}

	@JsonIgnore
	@AssertTrue(message = "la contrasena no debe superar 72 bytes en UTF-8")
	public boolean isPasswordDentroDeLimiteBcrypt() {
		return password == null || password.getBytes(StandardCharsets.UTF_8).length
				<= RestriccionesUsuario.LONGITUD_MAXIMA_PASSWORD_BCRYPT_BYTES;
	}

	@Override
	public String toString() {
		return "LoginRequest[email=" + email + ", password=***]";
	}

}