package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

  List<Menu> findByChef_User_Username(String username);

  @Query("SELECT DISTINCT m FROM Menu m " +
    "LEFT JOIN FETCH m.dishes " +
    "WHERE m.chef.id = :chefId")
  List<Menu> findByChefIdWithDishes(@Param("chefId") Long chefId);

  @Query("SELECT DISTINCT m FROM Menu m " +
    "LEFT JOIN FETCH m.dishes " +
    "LEFT JOIN FETCH m.chef c " +
    "LEFT JOIN FETCH c.user u " +
    "WHERE (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
    "AND (:description IS NULL OR LOWER(m.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
    "AND (:chefUsername IS NULL OR LOWER(u.username) = LOWER(:chefUsername)) " +
    "AND (:pickUpAvailable IS NULL OR c.pickUpAvailable = :pickUpAvailable) " +
    "AND (:deliveryAvailable IS NULL OR c.deliveryAvailable = :deliveryAvailable) " +
    "AND (:cookAtClientHome IS NULL OR c.cookAtClientHome = :cookAtClientHome)")
  List<Menu> findAllWithDishesAndFilters(
    @Param("title") String title,
    @Param("description") String description,
    @Param("chefUsername") String chefUsername,
    @Param("pickUpAvailable") Boolean pickUpAvailable,
    @Param("deliveryAvailable") Boolean deliveryAvailable,
    @Param("cookAtClientHome") Boolean cookAtClientHome
  );

  // Nuevo método para obtener todos los menús sin filtros
  @Query("SELECT DISTINCT m FROM Menu m " +
    "LEFT JOIN FETCH m.dishes " +
    "LEFT JOIN FETCH m.chef c " +
    "LEFT JOIN FETCH c.user u")
  List<Menu> findAllWithDishes();

  @Query("SELECT m FROM Menu m " +
    "LEFT JOIN FETCH m.dishes " +
    "LEFT JOIN FETCH m.chef c " +
    "LEFT JOIN FETCH c.user " +
    "WHERE m.id = :menuId")
  Optional<Menu> findByIdWithDetails(@Param("menuId") Long menuId);
}
