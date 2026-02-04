package com.chefpro.backendjava.repository;


import com.chefpro.backendjava.common.object.entity.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlatoRepository extends JpaRepository<Plato, Long> {


  // Método con query personalizada para verificar que los platos pertenecen al chef
  @Query("SELECT p FROM Plato p WHERE p.chefId = :chefUsername AND p.id IN :ids")
  List<Plato> findByChefUsernameAndIdIn(
    @Param("chefUsername") Long chefId,
    @Param("ids") List<Long> ids
  );
}

