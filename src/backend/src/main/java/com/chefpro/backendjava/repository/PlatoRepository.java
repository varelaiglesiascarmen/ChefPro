package com.chefpro.backendjava.repository;


import com.chefpro.backendjava.common.object.entity.Plato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatoRepository extends JpaRepository<Plato, String> {
    List<Plato> findByChefUsername(String chefUsername);
  }

