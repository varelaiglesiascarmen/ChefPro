package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reservation, Reservation.ReservationId> {

  // Buscar reservas por chef ID
  @Query("SELECT r FROM Reservation r WHERE r.chefId = :chefId")
  List<Reservation> findByChefId(@Param("chefId") Long chefId);

  // Buscar reservas por diner ID
  @Query("SELECT r FROM Reservation r WHERE r.diner.id = :dinerId")
  List<Reservation> findByDinerId(@Param("dinerId") Long dinerId);

  // Buscar reservas por chef y estado
  @Query("SELECT r FROM Reservation r WHERE r.chefId = :chefId AND r.status = :status")
  List<Reservation> findByChefIdAndStatus(
    @Param("chefId") Long chefId,
    @Param("status") Reservation.ReservationStatus status
  );

  // Buscar reservas por diner y estado
  @Query("SELECT r FROM Reservation r WHERE r.diner.id = :dinerId AND r.status = :status")
  List<Reservation> findByDinerIdAndStatus(
    @Param("dinerId") Long dinerId,
    @Param("status") Reservation.ReservationStatus status
  );

  // Buscar reserva específica con fetch del menú (para update)
  @Query("""
    SELECT r
    FROM Reservation r
    JOIN FETCH r.menu m
    JOIN FETCH r.chef c
    JOIN FETCH r.diner d
    WHERE r.chefId = :chefId
      AND r.date = :date
  """)
  Optional<Reservation> findByChefIdAndDateWithDetails(
    @Param("chefId") Long chefId,
    @Param("date") LocalDate date
  );

  // Buscar reservas de un chef en un rango de fechas
  @Query("SELECT r FROM Reservation r WHERE r.chefId = :chefId AND r.date BETWEEN :startDate AND :endDate")
  List<Reservation> findByChefIdAndDateBetween(
    @Param("chefId") Long chefId,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate
  );

  // Buscar reservas de un menú específico
  @Query("SELECT r FROM Reservation r WHERE r.menu.id = :menuId")
  List<Reservation> findByMenuId(@Param("menuId") Long menuId);

  // Verificar si un chef tiene disponibilidad en una fecha
  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reservation r WHERE r.chefId = :chefId AND r.date = :date")
  boolean existsByChefIdAndDate(
    @Param("chefId") Long chefId,
    @Param("date") LocalDate date
  );
}
