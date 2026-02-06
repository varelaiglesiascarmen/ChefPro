package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.common.object.entity.UserLogin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ChefRepository extends JpaRepository<Chef, Long> {

  Optional<Chef> findByUser_Username(String username);

  Optional<Chef> findByUser(UserLogin user);

  /**
   * Busca chefs por nombre (en nombre o apellido del usuario)
   */
  @Query("SELECT c FROM Chef c " +
          "WHERE LOWER(c.user.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
          "OR LOWER(c.user.lastname) LIKE LOWER(CONCAT('%', :name, '%'))")
  Page<Chef> findByNameContaining(@Param("name") String name, Pageable pageable);

  /**
   * Busca chefs disponibles en una fecha específica
   * (chefs que NO tienen reserva confirmada o pendiente en esa fecha)
   */
  @Query("SELECT c FROM Chef c " +
          "WHERE NOT EXISTS (" +
          "    SELECT r FROM Reservation r " +
          "    WHERE r.chef.id = c.id " +
          "    AND r.date = :date " +
          "    AND r.status IN ('PENDING', 'CONFIRMED')" +
          ")")
  Page<Chef> findAvailableOnDate(@Param("date") LocalDate date, Pageable pageable);

  /**
   * Busca chefs por nombre Y disponibles en una fecha
   */
  @Query("SELECT c FROM Chef c " +
          "WHERE (LOWER(c.user.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
          "OR LOWER(c.user.lastname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
          "AND NOT EXISTS (" +
          "    SELECT r FROM Reservation r " +
          "    WHERE r.chef.id = c.id " +
          "    AND r.date = :date " +
          "    AND r.status IN ('PENDING', 'CONFIRMED')" +
          ")")
  Page<Chef> findByNameAndAvailableOnDate(
          @Param("name") String name,
          @Param("date") LocalDate date,
          Pageable pageable);
}
