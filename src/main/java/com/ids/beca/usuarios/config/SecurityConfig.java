package com.ids.beca.usuarios.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.ids.beca.usuarios.domain.Rol;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String PREFIJO_AUTORIDAD = "ROLE_";
	private static final String RUTA_LOGIN = "/api/auth/login";
	private static final String RUTA_USUARIOS = "/api/usuarios";
	private static final String RUTA_USUARIO_POR_ID = "/api/usuarios/{id}";
	private static final String RUTA_HEALTH = "/actuator/health";

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, ManejadorErroresSeguridad manejador,
			JwtAuthenticationConverter conversor) throws Exception {
		// CSRF se deshabilita: API stateless, el token va en Authorization y no hay cookies de sesión.
		http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sesion -> sesion.sessionCreationPolicy(STATELESS))
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.exceptionHandling(errores -> errores
						.authenticationEntryPoint(manejador)
						.accessDeniedHandler(manejador))
				.oauth2ResourceServer(recurso -> recurso
						.authenticationEntryPoint(manejador)
						.accessDeniedHandler(manejador)
						.jwt(jwt -> jwt.jwtAuthenticationConverter(conversor)));
		reglasDeAcceso(http);
		return http.build();
	}

	private static void reglasDeAcceso(HttpSecurity http) throws Exception {
		String admin = Rol.ADMIN.name();
		http.authorizeHttpRequests(reglas -> reglas
				.requestMatchers(HttpMethod.POST, RUTA_LOGIN, RUTA_USUARIOS).permitAll()
				.requestMatchers(HttpMethod.GET, RUTA_HEALTH).permitAll()
				.requestMatchers(HttpMethod.GET, RUTA_USUARIOS, RUTA_USUARIO_POR_ID).hasRole(admin)
				.requestMatchers(HttpMethod.PUT, RUTA_USUARIO_POR_ID).hasRole(admin)
				.requestMatchers(HttpMethod.DELETE, RUTA_USUARIO_POR_ID).hasRole(admin)
				.anyRequest().denyAll());
	}

	@Bean
	public ManejadorErroresSeguridad manejadorErroresSeguridad() {
		return new ManejadorErroresSeguridad();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
		autoridades.setAuthoritiesClaimName(RolesClaimValidator.CLAIM_ROLES);
		autoridades.setAuthorityPrefix(PREFIJO_AUTORIDAD);
		JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
		conversor.setJwtGrantedAuthoritiesConverter(autoridades);
		return conversor;
	}

}
