package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.dto.MenuSearchProjection;
import com.chefpro.backendjava.common.object.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MenuSearchRepository extends JpaRepository<Menu, Long> {

  @Query(value = """
    SELECT
      m.menu_ID                   AS menuId,
      m.title                     AS menuTitle,
      m.description               AS menuDescription,
      m.price_per_person          AS pricePerPerson,
      m.min_number_diners         AS minDiners,
      m.max_number_diners         AS maxDiners,
      c.user_ID                   AS chefId,
      u.name                      AS chefName,
      u.lastname                  AS chefLastname,
      c.photo                     AS chefPhoto,
      c.location                  AS chefLocation,
      AVG(r.score)                AS avgScore,
      COUNT(DISTINCT r.review_ID) AS reviewsCount
    FROM menu m
    JOIN chefs c ON c.user_ID = m.chef_ID
    JOIN users u ON u.user_ID = c.user_ID
    LEFT JOIN reviews r ON r.reviewed_user = u.user_ID
    WHERE
      -- Búsqueda por texto libre sobre título y descripción del menú
      (:q IS NULL OR :q = '' OR
        LOWER(m.title)        LIKE CONCAT('%', LOWER(:q), '%') OR
        LOWER(m.description)  LIKE CONCAT('%', LOWER(:q), '%')
      )
      -- Filtro de precio mínimo
      AND (:minPrice IS NULL OR m.price_per_person >= :minPrice)
      -- Filtro de precio máximo
      AND (:maxPrice IS NULL OR m.price_per_person <= :maxPrice)
      -- Filtro de comensales
      AND (:guests IS NULL OR (m.min_number_diners IS NULL OR m.min_number_diners <= :guests))
      AND (:guests IS NULL OR (m.max_number_diners IS NULL OR m.max_number_diners >= :guests))
      -- Filtro de disponibilidad: el chef del menú no tiene reserva confirmada ese día
      AND (
        :date IS NULL OR NOT EXISTS (
          SELECT 1 FROM reservations res
          WHERE res.chef_ID = c.user_ID
            AND res.date = :date
            AND res.status = 'CONFIRMED'
        )
      )
      -- Filtro de alérgenos: excluir menús que contengan alguno de los alérgenos indicados
      AND (
        :#{#allergens == null || #allergens.isEmpty()} = true
        OR NOT EXISTS (
          SELECT 1
          FROM dishes d
          JOIN allergens_dishes ad ON ad.menu_ID = d.menu_ID AND ad.dish_ID = d.dish_ID
          WHERE d.menu_ID = m.menu_ID
            AND ad.allergen IN (:allergens)
        )
      )
    GROUP BY
      m.menu_ID, m.title, m.description, m.price_per_person,
      m.min_number_diners, m.max_number_diners,
      c.user_ID, u.name, u.lastname, c.photo, c.location
    ORDER BY avgScore DESC
    """,
    nativeQuery = true
  )
  List<MenuSearchProjection> searchMenus(
    @Param("q") String q,
    @Param("date") LocalDate date,
    @Param("minPrice") BigDecimal minPrice,
    @Param("maxPrice") BigDecimal maxPrice,
    @Param("guests") Integer guests,
    @Param("allergens") List<String> allergens
  );
<<<<<<< HEAD
=======

  @Query(value = """
    SELECT
      m.menu_ID                   AS menuId,
      m.title                     AS menuTitle,
      m.description               AS menuDescription,
      m.price_per_person          AS pricePerPerson,
      m.min_number_diners         AS minDiners,
      m.max_number_diners         AS maxDiners,
      c.user_ID                   AS chefId,
      u.name                      AS chefName,
      u.lastname                  AS chefLastname,
      c.photo                     AS chefPhoto,
      c.location                  AS chefLocation,
      AVG(r.score)                AS avgScore,
      COUNT(DISTINCT r.review_ID) AS reviewsCount
    FROM menu m
    JOIN chefs c ON c.user_ID = m.chef_ID
    JOIN users u ON u.user_ID = c.user_ID
    LEFT JOIN reviews r ON r.reviewed_user = u.user_ID
    GROUP BY
      m.menu_ID, m.title, m.description, m.price_per_person,
      m.min_number_diners, m.max_number_diners,
      c.user_ID, u.name, u.lastname, c.photo, c.location
    ORDER BY RAND()
    LIMIT :limit
    """,
    nativeQuery = true
  )
  List<MenuSearchProjection> findRandomMenuSuggestions(@Param("limit") int limit);
>>>>>>> 92e126861fcf8bdb5428abe2ca3b3b2043c4af64
}
