package com.ids.beca.usuarios.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.ids.beca.usuarios.domain.Rol;
import com.ids.beca.usuarios.domain.Usuario;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void buscaPorCorreoNormalizadoMedianteLaConsultaDelRepositorio() {
		jdbcTemplate.update("insert into usuarios (nombre, email, password_hash, rol, created_at) "
				+ "values (?, ?, ?, ?, CURRENT_TIMESTAMP)",
				"Ana", "ana@example.com", "hash-interno", Rol.SOLICITANTE.name());

		assertThat(usuarioRepository.buscarPorEmailNormalizado("ana@example.com"))
				.isPresent()
				.get()
				.extracting(Usuario::getEmail)
				.isEqualTo("ana@example.com");
	}

}