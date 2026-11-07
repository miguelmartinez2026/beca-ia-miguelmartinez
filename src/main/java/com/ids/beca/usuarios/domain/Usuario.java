package com.ids.beca.usuarios.domain;

import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre", nullable = false, length = RestriccionesUsuario.LONGITUD_MAXIMA_NOMBRE)
	private String nombre;

	@Column(name = "email", nullable = false, length = RestriccionesUsuario.LONGITUD_MAXIMA_EMAIL)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "rol", nullable = false, length = RestriccionesUsuario.LONGITUD_MAXIMA_ROL)
	private Rol rol;

	// Valor asignado por el DEFAULT now() de PostgreSQL; Hibernate lo recupera tras el INSERT.
	@Generated(event = EventType.INSERT)
	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	protected Usuario() {
	}

	public Usuario(String nombre, String email, String passwordHash, Rol rol) {
		this.nombre = nombre;
		this.email = email;
		this.passwordHash = passwordHash;
		this.rol = rol;
	}

	public void actualizarDatos(String nombre, String email) {
		this.nombre = nombre;
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getEmail() {
		return email;
	}

	public Rol getRol() {
		return rol;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
