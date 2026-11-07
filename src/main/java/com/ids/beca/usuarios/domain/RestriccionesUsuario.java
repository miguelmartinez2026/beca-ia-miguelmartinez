package com.ids.beca.usuarios.domain;

/**
 * Límites de longitud de los campos de usuario. Deben coincidir con las columnas
 * definidas en la migración Flyway V1__create_usuarios.sql.
 */
public final class RestriccionesUsuario {

	public static final int LONGITUD_MAXIMA_NOMBRE = 120;
	public static final int LONGITUD_MAXIMA_EMAIL = 254;
	public static final int LONGITUD_MAXIMA_ROL = 20;

	private RestriccionesUsuario() {
	}

}
