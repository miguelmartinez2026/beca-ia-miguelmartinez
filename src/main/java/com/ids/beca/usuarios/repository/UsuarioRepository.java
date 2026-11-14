package com.ids.beca.usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ids.beca.usuarios.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	@Query("select u from Usuario u where lower(u.email) = :email")
	Optional<Usuario> buscarPorEmailNormalizado(@Param("email") String email);
}
