package com.ids.beca.usuarios.dto;

import java.time.Instant;

import com.ids.beca.usuarios.domain.Rol;
import com.ids.beca.usuarios.domain.Usuario;

public record UsuarioResponse(Long id, String nombre, String email, Rol rol, Instant createdAt) {

	public static UsuarioResponse desde(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNombre(),
				usuario.getEmail(),
				usuario.getRol(),
				usuario.getCreatedAt());
	}

}
