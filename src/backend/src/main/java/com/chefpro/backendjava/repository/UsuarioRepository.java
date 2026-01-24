package com.chefpro.backendjava.repository;
import com.chefpro.backendjava.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Para Spring Security: buscar por username
    Optional<Usuario> findByUsername(String username);
}
