package com.ids.beca.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.ids.beca.usuarios.config.JwtProperties;
import com.ids.beca.usuarios.domain.Rol;
import com.ids.beca.usuarios.domain.Usuario;
import com.ids.beca.usuarios.dto.LoginRequest;
import com.ids.beca.usuarios.dto.LoginResponse;
import com.ids.beca.usuarios.exception.CredencialesInvalidasException;
import com.ids.beca.usuarios.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private TokenService tokenService;

	@Mock
	private JwtProperties jwtProperties;

	@InjectMocks
	private AuthService authService;

	@Test
	void autenticaCorreoNormalizadoYDevuelveTokenBearer() {
		Usuario usuario = usuarioConId(12L);
		when(usuarioRepository.buscarPorEmailNormalizado("ana@example.com")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("Clave-De-Prueba-1", "hash-interno")).thenReturn(true);
		when(tokenService.emitir(12L, Rol.ADMIN)).thenReturn("jwt-firmado");
		when(jwtProperties.expiracionSegundos()).thenReturn(3600L);

		LoginResponse response = authService.autenticar(
				new LoginRequest("  Ana@Example.COM ", "Clave-De-Prueba-1"));

		assertThat(response.accessToken()).isEqualTo("jwt-firmado");
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.expiresIn()).isEqualTo(3600);
		verify(usuarioRepository).buscarPorEmailNormalizado("ana@example.com");
		verify(passwordEncoder).matches("Clave-De-Prueba-1", "hash-interno");
        verify(tokenService).emitir(12L, Rol.ADMIN);
	}

	@Test
	void usuarioInexistenteProduceErrorGenericoSinEmitirToken() {
		when(usuarioRepository.buscarPorEmailNormalizado("ana@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.autenticar(
				new LoginRequest("ana@example.com", "Clave-De-Prueba-1")))
				.isInstanceOf(CredencialesInvalidasException.class)
				.hasMessage("Credenciales invalidas.");
		verifyNoInteractions(passwordEncoder, tokenService);
	}

	@Test
	void contrasenaIncorrectaProduceElMismoErrorGenerico() {
		when(usuarioRepository.buscarPorEmailNormalizado("ana@example.com"))
				.thenReturn(Optional.of(usuarioConId(12L)));
		when(passwordEncoder.matches("incorrecta", "hash-interno")).thenReturn(false);

		assertThatThrownBy(() -> authService.autenticar(
				new LoginRequest("ana@example.com", "incorrecta")))
				.isInstanceOf(CredencialesInvalidasException.class)
				.hasMessage("Credenciales invalidas.");
		verifyNoInteractions(tokenService);
	}

	private static Usuario usuarioConId(Long id) {
		Usuario usuario = new Usuario("Ana", "ana@example.com", "hash-interno", Rol.ADMIN);
		ReflectionTestUtils.setField(usuario, "id", id);
		return usuario;
	}

}