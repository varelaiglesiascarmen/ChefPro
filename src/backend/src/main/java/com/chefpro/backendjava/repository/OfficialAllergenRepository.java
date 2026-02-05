package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.OfficialAllergen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficialAllergenRepository extends JpaRepository<OfficialAllergen, String> {
  // La clave primaria es String (allergen_name)
}
