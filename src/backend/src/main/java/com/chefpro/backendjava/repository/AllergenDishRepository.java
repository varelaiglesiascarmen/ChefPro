package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.AllergenDish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergenDishRepository extends JpaRepository<AllergenDish, AllergenDish.AllergenDishId> {
}
