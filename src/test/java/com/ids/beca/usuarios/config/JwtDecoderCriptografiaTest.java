package com.ids.beca.usuarios.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.ids.beca.usuarios.support.ClaveJwtPruebas;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;

/**
 * Pruebas criptográficas contra el {@link NimbusJwtDecoder} real que construye {@link JwtConfig}.
 * El reloj es fijo para que los resultados no dependan del reloj del sistema.
 */
class JwtDecoderCriptografiaTest {

	private static final String EMISOR = "ms-usuarios";
	private static final Instant AHORA = Instant.parse("2026-11-10T12:00:00Z");
	private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);
	private static final SecureRandom ALEATORIO = new SecureRandom();

	private final NimbusJwtDecoder decoder = JwtConfig.crearDecoder(
			new SecretKeySpec(ClaveJwtPruebas.bytes(), "HmacSHA256"), EMISOR, RELOJ);

	@Test
	void aceptaJwtHs256ConFirmaCorrectaYClaimsDelContrato() throws Exception {
		Jwt jwt = decoder.decode(firmarHs256(cargaValida()));

		assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
		assertThat(jwt.getClaimAsString("iss")).isEqualTo(EMISOR);
		assertThat(jwt.getSubject()).isEqualTo("42");
		assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
	}

	@Test
	void rechazaPayloadManipuladoConLaFirmaOriginal() throws Exception {
		String[] original = partes(firmarHs256(cargaCon("roles", List.of("SOLICITANTE"))));
		String[] escalado = partes(firmarHs256(cargaCon("roles", List.of("ADMIN"))));
		String tokenManipulado = original[0] + "." + escalado[1] + "." + original[2];

		assertThatThrownBy(() -> decoder.decode(tokenManipulado)).isInstanceOf(JwtException.class);
	}

	@Test
	void rechazaFirmaAlteradaEnUnByte() throws Exception {
		String[] partes = partes(firmarHs256(cargaValida()));
		String firma = partes[2];
		char primero = firma.charAt(0);
		String firmaAlterada = (primero == 'A' ? 'B' : 'A') + firma.substring(1);
		String tokenAlterado = partes[0] + "." + partes[1] + "." + firmaAlterada;

		assertThatThrownBy(() -> decoder.decode(tokenAlterado)).isInstanceOf(JwtException.class);
	}

	@Test
	void rechazaJwtFirmadoConOtraClaveDeLaMismaLongitud() throws Exception {
		String token = firmar(cargaValida(), JWSAlgorithm.HS256, bytesAleatorios(32));

		assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
	}

	@ParameterizedTest(name = "rechaza algoritmo {0} aunque la clave tenga la longitud requerida")
	@CsvSource({"HS384, 48", "HS512, 64"})
	void rechazaAlgoritmosDistintosDeHs256(String algoritmo, int longitudClave) throws Exception {
		String token = firmar(cargaValida(), JWSAlgorithm.parse(algoritmo), bytesAleatorios(longitudClave));

		assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
	}

	@Test
	void rechazaTokenSinFirmaConAlgoritmoNone() throws ParseException {
		String token = new PlainJWT(cargaValidaComoClaims()).serialize();

		assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
	}

	@ParameterizedTest(name = "exp = ahora {0} s ⇒ aceptado={1}")
	@CsvSource({"1, true", "0, false", "-1, false"})
	void aplicaElLimiteExactoDeExp(long desplazamiento, boolean aceptado) throws Exception {
		String token = firmarHs256(cargaCon("exp", AHORA.plusSeconds(desplazamiento).getEpochSecond()));

		if (aceptado) {
			assertThatCode(() -> decoder.decode(token)).doesNotThrowAnyException();
		} else {
			assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
		}
	}

	@Test
	void rechazaTokenExpiradoHaceUnaHora() throws Exception {
		Map<String, Object> carga = cargaValida();
		carga.put("iat", AHORA.minusSeconds(7200).getEpochSecond());
		carga.put("exp", AHORA.minusSeconds(3600).getEpochSecond());

		assertThatThrownBy(() -> decoder.decode(firmarHs256(carga))).isInstanceOf(JwtException.class);
	}

	@ParameterizedTest(name = "rechaza claim inválido: {0}")
	@MethodSource("cargasInvalidas")
	void rechazaClaimsObligatoriosAusentesOInvalidos(String descripcion, Map<String, Object> carga) {
		String token = firmarHs256(carga);

		assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
	}

	static Stream<Arguments> cargasInvalidas() {
		return Stream.of(
				Arguments.of("sin exp", cargaSin("exp")),
				Arguments.of("sin iat", cargaSin("iat")),
				Arguments.of("sin sub", cargaSin("sub")),
				Arguments.of("sub en blanco", cargaCon("sub", " ")),
				Arguments.of("sin iss", cargaSin("iss")),
				Arguments.of("iss de otro emisor", cargaCon("iss", "otro-emisor")),
				Arguments.of("sin roles", cargaSin("roles")),
				Arguments.of("roles vacío", cargaCon("roles", List.of())),
				Arguments.of("rol con prefijo ROLE_", cargaCon("roles", List.of("ROLE_ADMIN"))),
				Arguments.of("roles como cadena", cargaCon("roles", "ADMIN")));
	}

	private static Map<String, Object> cargaValida() {
		Map<String, Object> carga = new HashMap<>();
		carga.put("iss", EMISOR);
		carga.put("sub", "42");
		carga.put("roles", List.of("ADMIN"));
		carga.put("iat", AHORA.getEpochSecond());
		carga.put("exp", AHORA.plusSeconds(3600).getEpochSecond());
		return carga;
	}

	private static Map<String, Object> cargaCon(String claim, Object valor) {
		Map<String, Object> carga = cargaValida();
		carga.put(claim, valor);
		return carga;
	}

	private static Map<String, Object> cargaSin(String claim) {
		Map<String, Object> carga = cargaValida();
		carga.remove(claim);
		return carga;
	}

	private static JWTClaimsSet cargaValidaComoClaims() {
		try {
			return JWTClaimsSet.parse(cargaValida());
		} catch (ParseException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static String firmarHs256(Map<String, Object> carga) {
		return firmar(carga, JWSAlgorithm.HS256, ClaveJwtPruebas.bytes());
	}

	private static String firmar(Map<String, Object> carga, JWSAlgorithm algoritmo, byte[] clave) {
		try {
			SignedJWT jwt = new SignedJWT(new JWSHeader(algoritmo), JWTClaimsSet.parse(carga));
			jwt.sign(new MACSigner(clave));
			return jwt.serialize();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static String[] partes(String token) {
		return token.split("\\.", -1);
	}

	private static byte[] bytesAleatorios(int longitud) {
		byte[] bytes = new byte[longitud];
		ALEATORIO.nextBytes(bytes);
		return bytes;
	}

}
