package com.ids.beca.usuarios.exception;

public class UsuarioNoEncontradoException extends RuntimeException {

	public UsuarioNoEncontradoException(Long id) {
		super("No existe un usuario con id " + id);
	}

}
