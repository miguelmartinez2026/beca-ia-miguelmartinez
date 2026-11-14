package com.ids.beca.usuarios.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import com.ids.beca.usuarios.config.JwtProperties;
import com.ids.beca.usuarios.domain.Rol;

@Service
public class TokenService {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;

	public TokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
		this.jwtEncoder = jwtEncoder;
		this.jwtProperties = jwtProperties;
	}

	public String emitir(Long usuarioId, Rol rol) {
		Instant ahora = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.issuer())
				.subject(usuarioId.toString())
				.claim("roles", List.of(rol.name()))
				.issuedAt(ahora)
				.expiresAt(ahora.plusSeconds(jwtProperties.expiracionSegundos()))
				.build();
		JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(encabezado, claims)).getTokenValue();
	}

}