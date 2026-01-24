package com.chefpro.backendjava.repository;
import com.chefpro.backendjava.repository.entity.CustomUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomUserRepository extends JpaRepository<CustomUser, Long> {

    // Para Spring Security: buscar por username
    Optional<CustomUser> findByUsername(String username);
}
