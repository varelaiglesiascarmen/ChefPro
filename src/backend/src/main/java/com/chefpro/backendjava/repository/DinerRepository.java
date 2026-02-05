package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.Diner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DinerRepository extends JpaRepository<Diner, Long> {

  // Buscar diner por username del usuario
  @Query("SELECT d FROM Diner d WHERE d.user.username = :username")
  Optional<Diner> findByUser_Username(@Param("username") String username);

  // Buscar diner por email del usuario
  @Query("SELECT d FROM Diner d WHERE d.user.email = :email")
  Optional<Diner> findByUser_Email(@Param("email") String email);

  // Verificar si existe un diner por user ID
  @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Diner d WHERE d.id = :userId")
  boolean existsByUserId(@Param("userId") Long userId);
}
