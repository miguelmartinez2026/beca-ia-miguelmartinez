package com.ids.beca.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.ids.beca.usuarios.config.SecurityConfig;
import com.ids.beca.usuarios.dto.LoginRequest;
import com.ids.beca.usuarios.dto.LoginResponse;
import com.ids.beca.usuarios.exception.CredencialesInvalidasException;
import com.ids.beca.usuarios.service.AuthService;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

	private static final String RUTA_LOGIN = "/api/auth/login";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void loginCorrectoDevuelveSoloElContratoDeRespuesta() throws Exception {
		when(authService.autenticar(any(LoginRequest.class)))
				.thenReturn(new LoginResponse("jwt-firmado", "Bearer", 3600));

		mockMvc.perform(post(RUTA_LOGIN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"ana@example.com","password":"Clave-De-Prueba-1"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("jwt-firmado"))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(3600))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void credencialesInvalidasResponden401GenericoEIdentico() throws Exception {
		when(authService.autenticar(any(LoginRequest.class))).thenThrow(new CredencialesInvalidasException());
		MvcResult usuarioInexistente = mockMvc.perform(solicitud("ana@example.com", "Clave-De-Prueba-1"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andReturn();
		MvcResult passwordIncorrecto = mockMvc.perform(solicitud("otra@example.com", "incorrecta"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andReturn();

		String body = usuarioInexistente.getResponse().getContentAsString();
		org.assertj.core.api.Assertions.assertThat(body)
				.isEqualTo(passwordIncorrecto.getResponse().getContentAsString())
				.doesNotContain("ana@example.com", "otra@example.com", "Clave-De-Prueba-1", "incorrecta", "jwt");
	}

	@Test
	void loginInvalidoDevuelve400SinLlamarAlServicio() throws Exception {
		mockMvc.perform(post(RUTA_LOGIN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":" ","password":" "}
						"""))
				.andExpect(status().isBadRequest());
		verifyNoInteractions(authService);
	}

	@Test
	void loginRechazaContrasenaDeMasDe72Bytes() throws Exception {
		String body = "{\"email\":\"ana@example.com\",\"password\":\"" + "a".repeat(73) + "\"}";

		mockMvc.perform(post(RUTA_LOGIN).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());
		verifyNoInteractions(authService);
	}

	private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder solicitud(
			String email, String password) {
		return post(RUTA_LOGIN).contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
	}

}