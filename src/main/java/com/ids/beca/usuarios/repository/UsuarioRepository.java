package com.ids.beca.usuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ids.beca.usuarios.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
