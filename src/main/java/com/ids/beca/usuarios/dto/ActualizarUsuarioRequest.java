package com.ids.beca.usuarios.dto;

import java.util.Locale;

import com.ids.beca.usuarios.domain.RestriccionesUsuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(
		@NotBlank @Size(max = RestriccionesUsuario.LONGITUD_MAXIMA_NOMBRE) String nombre,
		@NotBlank @Email @Size(max = RestriccionesUsuario.LONGITUD_MAXIMA_EMAIL) String email) {

	public ActualizarUsuarioRequest {
		email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
	}

}
