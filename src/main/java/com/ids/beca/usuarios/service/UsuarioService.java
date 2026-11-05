package com.ids.beca.usuarios.service;

import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ids.beca.usuarios.domain.Rol;
import com.ids.beca.usuarios.domain.Usuario;
import com.ids.beca.usuarios.dto.ActualizarUsuarioRequest;
import com.ids.beca.usuarios.dto.CrearUsuarioRequest;
import com.ids.beca.usuarios.dto.UsuarioResponse;
import com.ids.beca.usuarios.exception.EmailDuplicadoException;
import com.ids.beca.usuarios.exception.UsuarioNoEncontradoException;
import com.ids.beca.usuarios.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService {

	private static final String SQLSTATE_UNIQUE_VIOLATION = "23505";
	private static final String INDICE_EMAIL_UNICO = "uq_usuarios_email_lower";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public UsuarioResponse crear(CrearUsuarioRequest solicitud) {
		Usuario usuario = new Usuario(
				solicitud.nombre(),
				solicitud.email(),
				passwordEncoder.encode(solicitud.password()),
				Rol.SOLICITANTE);
		return UsuarioResponse.desde(guardar(usuario));
	}

	@Transactional(readOnly = true)
	public UsuarioResponse obtener(Long id) {
		return UsuarioResponse.desde(buscar(id));
	}

	@Transactional(readOnly = true)
	public List<UsuarioResponse> listar() {
		return usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
				.stream()
				.map(UsuarioResponse::desde)
				.toList();
	}

	public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest solicitud) {
		Usuario usuario = buscar(id);
		usuario.actualizarDatos(solicitud.nombre(), solicitud.email());
		return UsuarioResponse.desde(guardar(usuario));
	}

	public void eliminar(Long id) {
		if (!usuarioRepository.existsById(id)) {
			throw new UsuarioNoEncontradoException(id);
		}
		usuarioRepository.deleteById(id);
	}

	private Usuario buscar(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new UsuarioNoEncontradoException(id));
	}

	// El flush fuerza el INSERT/UPDATE aquí, para poder identificar el conflicto dentro de la operación.
	private Usuario guardar(Usuario usuario) {
		try {
			return usuarioRepository.saveAndFlush(usuario);
		} catch (DataIntegrityViolationException ex) {
			if (esViolacionDeEmailUnico(ex)) {
				throw new EmailDuplicadoException(ex);
			}
			throw ex;
		}
	}

	private boolean esViolacionDeEmailUnico(Throwable error) {
		for (Throwable causa = error; causa != null; causa = causa.getCause()) {
			if (causa instanceof ConstraintViolationException violacion) {
				return SQLSTATE_UNIQUE_VIOLATION.equals(violacion.getSQLState())
						&& INDICE_EMAIL_UNICO.equalsIgnoreCase(violacion.getConstraintName());
			}
		}
		return false;
	}

}
