package com.ids.beca.usuarios.exception;

public class EmailDuplicadoException extends RuntimeException {

	public EmailDuplicadoException(Throwable causa) {
		super("Ya existe un usuario con ese correo electrónico", causa);
	}

}
