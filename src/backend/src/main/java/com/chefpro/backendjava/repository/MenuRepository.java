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
    "LEFT JOIN FETCH c.user")
  List<Menu> findAllWithDishes();

  @Query("SELECT m FROM Menu m " +
    "LEFT JOIN FETCH m.dishes " +
    "LEFT JOIN FETCH m.chef c " +
    "LEFT JOIN FETCH c.user " +
    "WHERE m.id = :menuId")
  Optional<Menu> findByIdWithDetails(@Param("menuId") Long menuId);
}
