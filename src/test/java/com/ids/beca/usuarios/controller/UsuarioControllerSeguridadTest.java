package com.ids.beca.usuarios.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.ids.beca.usuarios.config.SecurityConfig;
import com.ids.beca.usuarios.domain.Rol;
import com.ids.beca.usuarios.dto.CrearUsuarioRequest;
import com.ids.beca.usuarios.dto.UsuarioResponse;
import com.ids.beca.usuarios.service.UsuarioService;

/**
 * Matriz de autorización con autenticación simulada ({@code jwt()}): no verifica firma ni expiración.
 */
@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerSeguridadTest {

	private static final String CUERPO_ACTUALIZACION = """
			{"nombre": "Nombre Nuevo", "email": "nuevo@example.com"}
			""";
	private static final String CUERPO_REGISTRO = """
			{"nombre": "Ana Prueba", "email": "ana@example.com", "password": "Clave-De-Prueba-1"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioService usuarioService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@ParameterizedTest
	@CsvSource({
			"GET, /api/usuarios",
			"GET, /api/usuarios/7",
			"PUT, /api/usuarios/7",
			"DELETE, /api/usuarios/7"
	})
	void sinTokenLosEndpointsAdministrativosDevuelven401Generico(String metodo, String ruta) throws Exception {
		mockMvc.perform(solicitud(metodo, ruta))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", "Bearer"))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.instance").doesNotExist())
				.andExpect(content().string(not(containsString(ruta))));
		verifyNoInteractions(usuarioService);
	}

	@ParameterizedTest
	@CsvSource({
			"GET, /api/usuarios, SOLICITANTE",
			"GET, /api/usuarios, AGENTE",
			"GET, /api/usuarios/7, SOLICITANTE",
			"GET, /api/usuarios/7, AGENTE",
			"PUT, /api/usuarios/7, SOLICITANTE",
			"PUT, /api/usuarios/7, AGENTE",
			"DELETE, /api/usuarios/7, SOLICITANTE",
			"DELETE, /api/usuarios/7, AGENTE"
	})
	void conRolInsuficienteLosEndpointsAdministrativosDevuelven403Generico(String metodo, String ruta, Rol rol)
			throws Exception {
		mockMvc.perform(solicitud(metodo, ruta).with(conRol(rol)))
				.andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.instance").doesNotExist())
				.andExpect(content().string(not(containsString(ruta))));
		verifyNoInteractions(usuarioService);
	}

	@ParameterizedTest
	@CsvSource({
			"GET, /api/usuarios, 200",
			"GET, /api/usuarios/7, 200",
			"PUT, /api/usuarios/7, 200",
			"DELETE, /api/usuarios/7, 204"
	})
	void conRolAdminLosEndpointsAdministrativosResponden(String metodo, String ruta, int estado) throws Exception {
		mockMvc.perform(solicitud(metodo, ruta).with(conRol(Rol.ADMIN)))
				.andExpect(status().is(estado));
	}

	@Test
	void tokenRechazadoPorElDecoderDevuelve401SinFiltrarElMotivo() throws Exception {
		when(jwtDecoder.decode("token-invalido"))
				.thenThrow(new BadJwtException("motivo interno del decoder"));

		mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer token-invalido"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", "Bearer"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(content().string(not(containsString("motivo interno del decoder"))))
				.andExpect(content().string(not(containsString("token-invalido"))));
	}

	@ParameterizedTest
	@EnumSource(Rol.class)
	void elRegistroPublicoResponde201ConCualquierRol(Rol rol) throws Exception {
		stubRegistro();

		mockMvc.perform(registro().with(conRol(rol)))
				.andExpect(status().isCreated());
	}

	@Test
	void elRegistroPublicoResponde201SinToken() throws Exception {
		stubRegistro();

		mockMvc.perform(registro())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.rol").value("SOLICITANTE"));
	}

	@Test
	void unaRutaNoDeclaradaDevuelve401AlAnonimoY403AlAutenticado() throws Exception {
		mockMvc.perform(get("/api/otra-ruta"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/otra-ruta").with(conRol(Rol.ADMIN)))
				.andExpect(status().isForbidden());
	}

	@Test
	void unMetodoNoDeclaradoDevuelve401AlAnonimoY403AlAdmin() throws Exception {
		mockMvc.perform(patch("/api/usuarios/7"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(patch("/api/usuarios/7").with(conRol(Rol.ADMIN)))
				.andExpect(status().isForbidden());
	}

	private void stubRegistro() {
		when(usuarioService.crear(any(CrearUsuarioRequest.class))).thenReturn(new UsuarioResponse(
				1L, "Ana Prueba", "ana@example.com", Rol.SOLICITANTE, Instant.parse("2026-11-04T12:00:00Z")));
	}

	private static MockHttpServletRequestBuilder registro() {
		return post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(CUERPO_REGISTRO);
	}

	private static MockHttpServletRequestBuilder solicitud(String metodo, String ruta) {
		return request(HttpMethod.valueOf(metodo), ruta)
				.contentType(MediaType.APPLICATION_JSON)
				.content(CUERPO_ACTUALIZACION);
	}

	private static RequestPostProcessor conRol(Rol rol) {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + rol.name()));
	}

}
