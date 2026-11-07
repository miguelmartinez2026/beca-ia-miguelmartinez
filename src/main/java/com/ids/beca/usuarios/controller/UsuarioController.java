package com.ids.beca.usuarios.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.ids.beca.usuarios.dto.ActualizarUsuarioRequest;
import com.ids.beca.usuarios.dto.CrearUsuarioRequest;
import com.ids.beca.usuarios.dto.UsuarioResponse;
import com.ids.beca.usuarios.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@PostMapping
	public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest solicitud) {
		UsuarioResponse creado = usuarioService.crear(solicitud);
		URI ubicacion = UriComponentsBuilder.fromPath("/api/usuarios/{id}").buildAndExpand(creado.id()).toUri();
		return ResponseEntity.created(ubicacion).body(creado);
	}

	@GetMapping("/{id}")
	public UsuarioResponse obtener(@PathVariable Long id) {
		return usuarioService.obtener(id);
	}

	@GetMapping
	public List<UsuarioResponse> listar() {
		return usuarioService.listar();
	}

	@PutMapping("/{id}")
	public UsuarioResponse actualizar(@PathVariable Long id,
			@Valid @RequestBody ActualizarUsuarioRequest solicitud) {
		return usuarioService.actualizar(id, solicitud);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Long id) {
		usuarioService.eliminar(id);
	}

}
