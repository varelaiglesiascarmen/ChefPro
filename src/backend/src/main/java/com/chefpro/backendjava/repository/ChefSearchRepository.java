package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.dto.ChefSearchDto;
import com.chefpro.backendjava.common.object.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ChefSearchRepository extends JpaRepository<Menu, Long> {

  @Query(value = """
    SELECT u.user_ID                 AS userId,
               u.username                AS username,
                   u.name                    AS name,
                       u.lastname                AS lastname,
      c.photo                   AS photo,
      c.bio                     AS bio,
      c.prizes                  AS prizes,
      MIN(m.price_per_person)   AS startingPrice,
      AVG(r.score)              AS avgScore,
      COUNT(r.review_ID)        AS reviewsCount
    FROM menu m
    JOIN chefs c ON c.user_ID = m.chef_ID
    JOIN users u ON u.user_ID = c.user_ID
    LEFT JOIN reviews r ON r.reviewed_user = u.user_ID
    WHERE
      (:q IS NULL OR :q = '' OR
        LOWER(u.username) LIKE CONCAT('%', LOWER(:q), '%') OR
        LOWER(u.name)     LIKE CONCAT('%', LOWER(:q), '%') OR
        LOWER(u.lastname) LIKE CONCAT('%', LOWER(:q), '%') OR
        LOWER(m.title)    LIKE CONCAT('%', LOWER(:q), '%') OR
        LOWER(m.description) LIKE CONCAT('%', LOWER(:q), '%')
      )
      AND (:minPrice IS NULL OR m.price_per_person >= :minPrice)
      AND (:maxPrice IS NULL OR m.price_per_person <= :maxPrice)
      AND (:guests IS NULL OR (m.min_number_diners IS NULL OR m.min_number_diners <= :guests))
      AND (:guests IS NULL OR (m.max_number_diners IS NULL OR m.max_number_diners >= :guests))
      AND (
        :date IS NULL OR NOT EXISTS (
          SELECT 1
          FROM reservations res
          WHERE res.chef_ID = u.user_ID
            AND res.date = :date
        )
      )
    GROUP BY
      u.user_ID, u.username, u.name, u.lastname, c.photo, c.bio, c.prizes
    """,
    countQuery = """
    SELECT COUNT(*) FROM (
      SELECT u.user_ID
      FROM menu m
      JOIN chefs c ON c.user_ID = m.chef_ID
      JOIN users u ON u.user_ID = c.user_ID
      WHERE
        (:q IS NULL OR :q = '' OR
          LOWER(u.username) LIKE CONCAT('%', LOWER(:q), '%') OR
          LOWER(u.name)     LIKE CONCAT('%', LOWER(:q), '%') OR
          LOWER(u.lastname) LIKE CONCAT('%', LOWER(:q), '%') OR
          LOWER(m.title)    LIKE CONCAT('%', LOWER(:q), '%') OR
          LOWER(m.description) LIKE CONCAT('%', LOWER(:q), '%')
        )
        AND (:minPrice IS NULL OR m.price_per_person >= :minPrice)
        AND (:maxPrice IS NULL OR m.price_per_person <= :maxPrice)
        AND (:guests IS NULL OR (m.min_number_diners IS NULL OR m.min_number_diners <= :guests))
        AND (:guests IS NULL OR (m.max_number_diners IS NULL OR m.max_number_diners >= :guests))
        AND (
          :date IS NULL OR NOT EXISTS (
            SELECT 1
            FROM reservations res
            WHERE res.chef_ID = u.user_ID
              AND res.date = :date
          )
        )
      GROUP BY u.user_ID
    ) x
    """,
    nativeQuery = true
  )
  Page<ChefSearchDto> searchChefs(
    @Param("q") String q,
    @Param("date") LocalDate date,
    @Param("minPrice") BigDecimal minPrice,
    @Param("maxPrice") BigDecimal maxPrice,
    @Param("guests") Integer guests,
    Pageable pageable
  );
}
