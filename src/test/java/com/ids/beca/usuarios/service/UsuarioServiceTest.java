package com.ids.beca.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.ids.beca.usuarios.domain.Rol;
import com.ids.beca.usuarios.domain.Usuario;
import com.ids.beca.usuarios.dto.ActualizarUsuarioRequest;
import com.ids.beca.usuarios.dto.CrearUsuarioRequest;
import com.ids.beca.usuarios.dto.UsuarioResponse;
import com.ids.beca.usuarios.exception.EmailDuplicadoException;
import com.ids.beca.usuarios.exception.UsuarioNoEncontradoException;
import com.ids.beca.usuarios.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UsuarioService usuarioService;

	@Test
	void crearUsuarioGuardaHashDeContrasenaRolSolicitanteYCorreoNormalizado() {
		// Arrange
		String contrasena = "Clave-De-Prueba-1";
		CrearUsuarioRequest solicitud = new CrearUsuarioRequest("Ana Prueba", "  Ana@Example.COM ", contrasena);
		when(passwordEncoder.encode(contrasena)).thenReturn("hash-simulado");
		when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		// Act
		usuarioService.crear(solicitud);

		// Assert
		verify(passwordEncoder).encode(contrasena);
		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).saveAndFlush(captor.capture());
		Usuario guardado = captor.getValue();
		assertThat(ReflectionTestUtils.getField(guardado, "passwordHash"))
				.isEqualTo("hash-simulado")
				.isNotEqualTo(contrasena);
		assertThat(guardado.getRol()).isEqualTo(Rol.SOLICITANTE);
		assertThat(guardado.getEmail()).isEqualTo(solicitud.email()).isEqualTo("ana@example.com");
	}

	@Test
	void crearUsuarioLanzaEmailDuplicadoCuandoPostgresReportaUnicidadSinNombreDeRestriccion() {
		// Arrange: SQLState 23505 con nombre de restricción null, como ocurre con mensajes localizados
		CrearUsuarioRequest solicitud = new CrearUsuarioRequest("Ana Prueba", "ana@example.com", "Clave-De-Prueba-1");
		SQLException errorSql = new SQLException("violación de unicidad simulada", "23505");
		ConstraintViolationException violacion =
				new ConstraintViolationException("violación de unicidad simulada", errorSql, null);
		DataIntegrityViolationException errorOriginal =
				new DataIntegrityViolationException("conflicto simulado", violacion);
		when(passwordEncoder.encode(any())).thenReturn("hash-simulado");
		when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenThrow(errorOriginal);

		// Act + Assert
		assertThatThrownBy(() -> usuarioService.crear(solicitud))
				.isInstanceOf(EmailDuplicadoException.class)
				.hasCause(errorOriginal);
	}

	@Test
	void actualizarUsuarioModificaSoloNombreYCorreoYConservaRolHashYFechaDeCreacion() {
		// Arrange
		Instant fechaCreacion = Instant.parse("2026-11-04T12:00:00Z");
		Usuario existente = new Usuario("Nombre Original", "original@example.com", "hash-original", Rol.AGENTE);
		ReflectionTestUtils.setField(existente, "id", 1L);
		ReflectionTestUtils.setField(existente, "createdAt", fechaCreacion);
		ActualizarUsuarioRequest solicitud = new ActualizarUsuarioRequest("Nombre Nuevo", "  NUEVO@Example.com ");
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		// Act
		UsuarioResponse respuesta = usuarioService.actualizar(1L, solicitud);

		// Assert
		assertThat(existente.getNombre()).isEqualTo("Nombre Nuevo");
		assertThat(existente.getEmail()).isEqualTo("nuevo@example.com");
		assertThat(existente.getRol()).isEqualTo(Rol.AGENTE);
		assertThat(ReflectionTestUtils.getField(existente, "passwordHash")).isEqualTo("hash-original");
		assertThat(existente.getCreatedAt()).isEqualTo(fechaCreacion);
		assertThat(respuesta.nombre()).isEqualTo("Nombre Nuevo");
		assertThat(respuesta.email()).isEqualTo("nuevo@example.com");
		verifyNoInteractions(passwordEncoder);
		verify(usuarioRepository).saveAndFlush(existente);
	}

	@Test
	void actualizarUsuarioInexistenteLanzaExcepcion() {
		when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());
		ActualizarUsuarioRequest solicitud = new ActualizarUsuarioRequest("Ana Prueba", "ana@example.com");

		assertThatThrownBy(() -> usuarioService.actualizar(1L, solicitud))
				.isInstanceOf(UsuarioNoEncontradoException.class);
		verify(usuarioRepository, never()).saveAndFlush(any(Usuario.class));
	}

	@Test
	void eliminarUsuarioInexistenteLanzaExcepcion() {
		when(usuarioRepository.existsById(1L)).thenReturn(false);

		assertThatThrownBy(() -> usuarioService.eliminar(1L))
				.isInstanceOf(UsuarioNoEncontradoException.class);
		verify(usuarioRepository, never()).deleteById(1L);
	}

	@Test
	void actualizarUsuarioConCorreoDuplicadoLanzaExcepcion() {
		Usuario existente = new Usuario("Ana Prueba", "ana@example.com", "hash-simulado", Rol.AGENTE);
		ActualizarUsuarioRequest solicitud = new ActualizarUsuarioRequest("Ana Prueba", "ocupado@example.com");
		SQLException errorSql = new SQLException("violación de unicidad simulada", "23505");
		ConstraintViolationException violacion =
				new ConstraintViolationException("correo duplicado", errorSql, "uq_usuarios_email_lower");
		DataIntegrityViolationException errorOriginal = new DataIntegrityViolationException("conflicto", violacion);
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(usuarioRepository.saveAndFlush(existente)).thenThrow(errorOriginal);

		assertThatThrownBy(() -> usuarioService.actualizar(1L, solicitud))
				.isInstanceOf(EmailDuplicadoException.class)
				.hasCause(errorOriginal);
	}

	@Test
	void listarUsuariosDevuelveRespuestasOrdenadasPorId() {
		// Arrange: el repositorio entrega los usuarios ya ordenados por ID.
		Instant fechaCreacion = Instant.parse("2026-11-04T12:00:00Z");
		Usuario primero = new Usuario("Ana Prueba", "ana@example.com", "hash-ana", Rol.SOLICITANTE);
		Usuario segundo = new Usuario("Luis Prueba", "luis@example.com", "hash-luis", Rol.AGENTE);
		ReflectionTestUtils.setField(primero, "id", 1L);
		ReflectionTestUtils.setField(segundo, "id", 2L);
		ReflectionTestUtils.setField(primero, "createdAt", fechaCreacion);
		ReflectionTestUtils.setField(segundo, "createdAt", fechaCreacion);
		Sort orden = Sort.by(Sort.Direction.ASC, "id");
		when(usuarioRepository.findAll(orden)).thenReturn(List.of(primero, segundo));

		// Act
		List<UsuarioResponse> respuestas = usuarioService.listar();

		// Assert
		assertThat(respuestas).hasSize(2);
		assertThat(respuestas).extracting(UsuarioResponse::id).containsExactly(1L, 2L);
		assertThat(respuestas).extracting(UsuarioResponse::email)
				.containsExactly("ana@example.com", "luis@example.com");
		verify(usuarioRepository).findAll(orden);
		verifyNoInteractions(passwordEncoder);
	}

	@Test
	void eliminarUsuarioExistenteLlamaDeleteById() {
		// Arrange
		when(usuarioRepository.existsById(2L)).thenReturn(true);

		// Act
		usuarioService.eliminar(2L);

		// Assert
		verify(usuarioRepository).existsById(2L);
		verify(usuarioRepository).deleteById(2L);
		verifyNoInteractions(passwordEncoder);
	}
}
