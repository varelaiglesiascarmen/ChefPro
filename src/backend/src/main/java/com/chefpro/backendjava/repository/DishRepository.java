package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Dish.DishId> {

  // Encontrar el máximo dish_ID dentro de un menú específico
  @Query("SELECT MAX(d.dishId) FROM Dish d WHERE d.menuId = :menuId")
  Optional<Long> findMaxDishIdByMenuId(@Param("menuId") Long menuId);

  // Encontrar todos los platos de un menú específico
  @Query("SELECT d FROM Dish d WHERE d.menuId = :menuId")
  List<Dish> findAllByMenuId(@Param("menuId") Long menuId);

  // Encontrar todos los platos de un chef (a través de sus menús)
  @Query("SELECT d FROM Dish d WHERE d.menu.chef.id = :chefId")
  List<Dish> findAllByChefId(@Param("chefId") Long chefId);

  // Verificar que los platos pertenecen a un chef específico
  @Query("SELECT d FROM Dish d WHERE d.menu.chef.id = :chefId AND d.menuId = :menuId AND d.dishId IN :dishIds")
  List<Dish> findByChefIdAndMenuIdAndDishIdIn(
    @Param("chefId") Long chefId,
    @Param("menuId") Long menuId,
    @Param("dishIds") List<Long> dishIds
  );

  // Buscar platos del chef cuyo título contenga el texto proporcionado
  @Query("SELECT d FROM Dish d WHERE d.menu.chef.id = :chefId AND LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))")
  List<Dish> findByChefIdAndTitleContaining(
    @Param("chefId") Long chefId,
    @Param("title") String title
  );
}
