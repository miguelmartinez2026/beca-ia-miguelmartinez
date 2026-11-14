package com.ids.beca.usuarios.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import com.ids.beca.usuarios.domain.Rol;

/**
 * Exige que el claim {@code roles} sea un arreglo no vacío de roles conocidos.
 */
public class RolesClaimValidator implements OAuth2TokenValidator<Jwt> {

	public static final String CLAIM_ROLES = "roles";

	private static final Set<String> ROLES_PERMITIDOS = Arrays.stream(Rol.values())
			.map(Rol::name)
			.collect(Collectors.toUnmodifiableSet());

	private static final OAuth2Error ERROR_ROLES =
			new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "El claim roles no es válido.", null);

	@Override
	public OAuth2TokenValidatorResult validate(Jwt jwt) {
		if (rolesValidos(jwt.getClaim(CLAIM_ROLES))) {
			return OAuth2TokenValidatorResult.success();
		}
		return OAuth2TokenValidatorResult.failure(ERROR_ROLES);
	}

	private static boolean rolesValidos(Object valor) {
		return valor instanceof Collection<?> roles
				&& !roles.isEmpty()
				&& roles.stream().allMatch(RolesClaimValidator::esRolConocido);
	}

	private static boolean esRolConocido(Object rol) {
		return rol instanceof String nombre && ROLES_PERMITIDOS.contains(nombre);
	}

}
