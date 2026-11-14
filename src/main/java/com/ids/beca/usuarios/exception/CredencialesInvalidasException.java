package com.ids.beca.usuarios.exception;

public class CredencialesInvalidasException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private static final String MENSAJE = "Credenciales invalidas.";

	public CredencialesInvalidasException() {
		super(MENSAJE);
	}

}