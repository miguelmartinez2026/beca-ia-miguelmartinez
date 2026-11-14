package com.ids.beca.usuarios.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ids.beca.usuarios.config.JwtProperties;
import com.ids.beca.usuarios.domain.Usuario;
import com.ids.beca.usuarios.dto.LoginRequest;
import com.ids.beca.usuarios.dto.LoginResponse;
import com.ids.beca.usuarios.exception.CredencialesInvalidasException;
import com.ids.beca.usuarios.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {

	private static final String TIPO_TOKEN = "Bearer";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;
	private final JwtProperties jwtProperties;

	public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
			TokenService tokenService, JwtProperties jwtProperties) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
		this.jwtProperties = jwtProperties;
	}

	public LoginResponse autenticar(LoginRequest solicitud) {
		String email = solicitud.email().strip().toLowerCase(Locale.ROOT);
		Usuario usuario = usuarioRepository.buscarPorEmailNormalizado(email)
				.orElseThrow(CredencialesInvalidasException::new);
		if (!passwordEncoder.matches(solicitud.password(), usuario.getPasswordHash())) {
			throw new CredencialesInvalidasException();
		}
		String token = tokenService.emitir(usuario.getId(), usuario.getRol());
		return new LoginResponse(token, TIPO_TOKEN, jwtProperties.expiracionSegundos());
	}

}