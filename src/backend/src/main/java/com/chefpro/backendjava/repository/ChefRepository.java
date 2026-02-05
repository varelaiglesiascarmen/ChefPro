package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.Chef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChefRepository extends JpaRepository<Chef, Long> {

  Optional<Chef> findByUser_Username(String username);

}
