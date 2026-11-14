package com.ids.beca.usuarios.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class SecurityConfigTest {

	private final JwtAuthenticationConverter conversor = new SecurityConfig().jwtAuthenticationConverter();

	@Test
	void convierteElClaimRolesEnAutoridadesConPrefijoRole() {
		Jwt jwt = jwtConRoles(List.of("AGENTE", "ADMIN"));

		List<String> autoridades = conversor.convert(jwt).getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();

		assertThat(autoridades).containsExactlyInAnyOrder("ROLE_AGENTE", "ROLE_ADMIN");
	}

	@Test
	void noGeneraAutoridadesDeScopeAunqueExistaElClaim() {
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "HS256")
				.claim("roles", List.of("SOLICITANTE"))
				.claim("scope", "admin")
				.build();

		List<String> autoridades = conversor.convert(jwt).getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();

		assertThat(autoridades).containsExactly("ROLE_SOLICITANTE");
	}

	private static Jwt jwtConRoles(List<String> roles) {
		return Jwt.withTokenValue("token")
				.header("alg", "HS256")
				.claim("roles", roles)
				.build();
	}

}
