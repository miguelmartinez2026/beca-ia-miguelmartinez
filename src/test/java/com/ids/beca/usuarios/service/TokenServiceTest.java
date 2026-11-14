package com.ids.beca.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Date;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.ids.beca.usuarios.config.JwtProperties;
import com.ids.beca.usuarios.domain.Rol;
import com.ids.beca.usuarios.support.ClaveJwtPruebas;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jwt.SignedJWT;

class TokenServiceTest {

	@Test
	void emiteJwtHs256ConClaimsExactosYVigenciaDeUnaHora() throws Exception {
		SecretKey key = new SecretKeySpec(ClaveJwtPruebas.bytes(), "HmacSHA256");
		JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
		JwtProperties properties = new JwtProperties(ClaveJwtPruebas.base64(), "ms-usuarios", 3600);
		TokenService tokenService = new TokenService(encoder, properties);

		SignedJWT jwt = SignedJWT.parse(tokenService.emitir(42L, Rol.ADMIN));

		assertThat(jwt.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.HS256);
		assertThat(jwt.verify(new MACVerifier(ClaveJwtPruebas.bytes()))).isTrue();
		assertThat(jwt.getJWTClaimsSet().getClaims().keySet())
				.containsExactlyInAnyOrder("iss", "sub", "roles", "iat", "exp");
		assertThat(jwt.getJWTClaimsSet().getIssuer()).isEqualTo("ms-usuarios");
		assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("42");
		assertThat(jwt.getJWTClaimsSet().getStringListClaim("roles")).containsExactly("ADMIN");
		Date issuedAt = jwt.getJWTClaimsSet().getIssueTime();
		Date expiresAt = jwt.getJWTClaimsSet().getExpirationTime();
		assertThat(Duration.between(issuedAt.toInstant(), expiresAt.toInstant()).getSeconds()).isEqualTo(3600);
		assertThat(MacAlgorithm.HS256.getName()).isEqualTo(jwt.getHeader().getAlgorithm().getName());
	}

}