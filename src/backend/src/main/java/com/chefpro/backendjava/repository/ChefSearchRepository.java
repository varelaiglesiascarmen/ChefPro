package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.dto.ChefSearchProjection;
import com.chefpro.backendjava.common.object.entity.Chef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ChefSearchRepository extends JpaRepository<Chef, Long> {

  @Query(value = """
    SELECT
      u.user_ID               AS userId,
      u.username              AS username,
      u.name                  AS name,
      u.lastname              AS lastname,
      c.photo                 AS photo,
      c.bio                   AS bio,
      c.location              AS location,
      AVG(r.score)            AS avgScore,
      COUNT(DISTINCT r.review_ID) AS reviewsCount,
      MIN(m.price_per_person) AS startingPrice
    FROM chefs c
    JOIN users u ON u.user_ID = c.user_ID
    LEFT JOIN menu m ON m.chef_ID = c.user_ID
    LEFT JOIN reviews r ON r.reviewed_user = u.user_ID
    WHERE
      -- Búsqueda por texto libre sobre nombre, apellido y username del chef
      (:q IS NULL OR :q = '' OR
        LOWER(u.name)      LIKE CONCAT('%', LOWER(:q), '%') OR
        LOWER(u.lastname)  LIKE CONCAT('%', LOWER(:q), '%') OR
        LOWER(u.username)  LIKE CONCAT('%', LOWER(:q), '%')
      )
      -- Filtro de disponibilidad: el chef no tiene reserva confirmada en esa fecha
      AND (
        :date IS NULL OR NOT EXISTS (
          SELECT 1 FROM reservations res
          WHERE res.chef_ID = c.user_ID
            AND res.date = :date
            AND res.status = 'CONFIRMED'
        )
      )
    GROUP BY
      u.user_ID, u.username, u.name, u.lastname, c.photo, c.bio, c.location
    ORDER BY avgScore DESC
    """,
    nativeQuery = true
  )
  List<ChefSearchProjection> searchChefs(
    @Param("q") String q,
    @Param("date") LocalDate date
  );
}
