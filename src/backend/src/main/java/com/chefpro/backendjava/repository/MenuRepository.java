package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    // Menús creados por un chef concreto
    List<Menu> findByChef_User_Username(String username);

}
