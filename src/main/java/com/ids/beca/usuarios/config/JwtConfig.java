package com.ids.beca.usuarios.config;

import java.time.Clock;
import java.time.Duration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

	private static final String ALGORITMO_CLAVE = "HmacSHA256";

	@Bean
	public SecretKey jwtSecretKey(JwtProperties propiedades) {
		return new SecretKeySpec(propiedades.claveDecodificada(), ALGORITMO_CLAVE);
	}

	@Bean
	public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
	}

	@Bean
	public JwtDecoder jwtDecoder(SecretKey jwtSecretKey, JwtProperties propiedades) {
		return crearDecoder(jwtSecretKey, propiedades.issuer(), Clock.systemUTC());
	}

	static NimbusJwtDecoder crearDecoder(SecretKey clave, String emisor, Clock reloj) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(clave)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		JwtTimestampValidator expiracion = new JwtTimestampValidator(Duration.ZERO);
		expiracion.setClock(reloj);
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				new JwtIssuerValidator(emisor),
				expiracion,
				claimObligatorio(JwtClaimNames.EXP),
				claimObligatorio(JwtClaimNames.IAT),
				subjectObligatorio(),
				new RolesClaimValidator()));
		return decoder;
	}

	private static OAuth2TokenValidator<Jwt> claimObligatorio(String claim) {
		return new JwtClaimValidator<Object>(claim, valor -> valor != null);
	}

	private static OAuth2TokenValidator<Jwt> subjectObligatorio() {
		return new JwtClaimValidator<Object>(JwtClaimNames.SUB,
				valor -> valor instanceof String subject && !subject.isBlank());
	}

}
