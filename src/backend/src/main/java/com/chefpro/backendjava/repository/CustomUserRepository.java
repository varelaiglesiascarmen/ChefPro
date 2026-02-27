package com.chefpro.backendjava.repository;
import com.chefpro.backendjava.common.object.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomUserRepository extends JpaRepository<UserLogin, Long> {

    // Para Spring Security: buscar por username
    Optional<UserLogin> findByUsername(String username);

    // Para validaciones de registro
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
