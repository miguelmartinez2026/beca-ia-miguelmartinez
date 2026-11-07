package com.ids.beca.usuarios.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ids.beca.usuarios.domain.Rol;
import com.ids.beca.usuarios.dto.ActualizarUsuarioRequest;
import com.ids.beca.usuarios.dto.CrearUsuarioRequest;
import com.ids.beca.usuarios.dto.UsuarioResponse;
import com.ids.beca.usuarios.exception.EmailDuplicadoException;
import com.ids.beca.usuarios.exception.UsuarioNoEncontradoException;
import com.ids.beca.usuarios.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

	private static final Instant FECHA_CREACION = Instant.parse("2026-11-04T12:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioService usuarioService;

	@Test
	void postValidoDevuelve201ConLocationRolSolicitanteYSinContrasenaNiHash() throws Exception {
		// Arrange
		when(usuarioService.crear(any(CrearUsuarioRequest.class)))
				.thenReturn(new UsuarioResponse(1L, "Ana Prueba", "ana@example.com", Rol.SOLICITANTE, FECHA_CREACION));
		String cuerpo = """
				{"nombre": "Ana Prueba", "email": "ana@example.com", "password": "Clave-De-Prueba-1"}
				""";

		// Act + Assert
		mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/usuarios/1"))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.nombre").value("Ana Prueba"))
				.andExpect(jsonPath("$.email").value("ana@example.com"))
				.andExpect(jsonPath("$.rol").value("SOLICITANTE"))
				.andExpect(jsonPath("$.createdAt").value("2026-11-04T12:00:00Z"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist())
				.andExpect(content().string(not(containsString("Clave-De-Prueba-1"))));
	}

	@Test
	void getPorIdExistenteDevuelve200ConLosDatosDelUsuario() throws Exception {
		// Arrange
		when(usuarioService.obtener(7L))
				.thenReturn(new UsuarioResponse(7L, "Luis Prueba", "luis@example.com", Rol.AGENTE, FECHA_CREACION));

		// Act + Assert
		mockMvc.perform(get("/api/usuarios/7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.nombre").value("Luis Prueba"))
				.andExpect(jsonPath("$.email").value("luis@example.com"))
				.andExpect(jsonPath("$.rol").value("AGENTE"));
	}

	@Test
	void getGeneralDevuelve200ConUnArregloDeUsuarios() throws Exception {
		// Arrange
		when(usuarioService.listar()).thenReturn(List.of(
				new UsuarioResponse(1L, "Ana Prueba", "ana@example.com", Rol.SOLICITANTE, FECHA_CREACION),
				new UsuarioResponse(2L, "Luis Prueba", "luis@example.com", Rol.AGENTE, FECHA_CREACION)));

		// Act + Assert
		mockMvc.perform(get("/api/usuarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[1].email").value("luis@example.com"));
	}

	@Test
	void putValidoDevuelve200ConNombreYCorreoActualizados() throws Exception {
		// Arrange
		when(usuarioService.actualizar(eq(3L), any(ActualizarUsuarioRequest.class)))
				.thenReturn(new UsuarioResponse(3L, "Nombre Nuevo", "nuevo@example.com", Rol.SOLICITANTE, FECHA_CREACION));
		String cuerpo = """
				{"nombre": "Nombre Nuevo", "email": "  NUEVO@Example.com "}
				""";

		// Act + Assert
		mockMvc.perform(put("/api/usuarios/3").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(3))
				.andExpect(jsonPath("$.nombre").value("Nombre Nuevo"))
				.andExpect(jsonPath("$.email").value("nuevo@example.com"));
		verify(usuarioService).actualizar(3L, new ActualizarUsuarioRequest("Nombre Nuevo", "nuevo@example.com"));
	}

	@Test
	void deleteExistenteDevuelve204SinContenido() throws Exception {
		// Arrange: el servicio simulado no lanza ninguna excepción

		// Act + Assert
		mockMvc.perform(delete("/api/usuarios/5"))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));
		verify(usuarioService).eliminar(5L);
	}

	@Test
	void postConNombreEnBlancoDevuelve400ConErrorDeValidacionSinLlamarAlServicio() throws Exception {
		// Arrange
		String cuerpo = """
				{"nombre": "   ", "email": "ana@example.com", "password": "Clave-De-Prueba-1"}
				""";

		// Act + Assert
		mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors[?(@.campo == 'nombre')]").isNotEmpty())
				.andExpect(content().string(not(containsString("Clave-De-Prueba-1"))));
		verifyNoInteractions(usuarioService);
	}

	@Test
	void getDeUsuarioInexistenteDevuelve404ConProblemDetail() throws Exception {
		// Arrange
		when(usuarioService.obtener(99L)).thenThrow(new UsuarioNoEncontradoException(99L));

		// Act + Assert
		mockMvc.perform(get("/api/usuarios/99"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("No existe un usuario con id 99"));
	}

	@Test
	void postConCorreoDuplicadoDevuelve409ConProblemDetailSinDatosSensibles() throws Exception {
		// Arrange
		when(usuarioService.crear(any(CrearUsuarioRequest.class)))
				.thenThrow(new EmailDuplicadoException(new RuntimeException("causa interna simulada")));
		String cuerpo = """
				{"nombre": "Ana Prueba", "email": "ana@example.com", "password": "Clave-De-Prueba-1"}
				""";

		// Act + Assert
		mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail").value("Ya existe un usuario con ese correo electrónico"))
				.andExpect(content().string(not(containsString("Clave-De-Prueba-1"))))
				.andExpect(content().string(not(containsString("causa interna simulada"))));
	}

}
