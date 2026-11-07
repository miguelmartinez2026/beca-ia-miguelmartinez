package com.ids.beca.usuarios.dto;

import java.util.Locale;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(
		@NotBlank @Size(max = 120) String nombre,
		@NotBlank @Email @Size(max = 254) String email) {

	public ActualizarUsuarioRequest {
		email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
	}

}
