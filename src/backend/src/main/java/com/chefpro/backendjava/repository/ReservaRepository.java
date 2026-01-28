package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.reservations;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<reservations, Long> {

    // Ver reservas de un comensal
    List<reservations> findByComensalUsername(String comensalUsername);

    // (Opcional) Ver reservas de un chef
    List<reservations> findByChefUsername(String chefUsername);
}
