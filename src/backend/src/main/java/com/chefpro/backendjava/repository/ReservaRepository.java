package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Ver reservas de un comensal
    List<Reserva> findByComensalUsername(String comensalUsername);

    // (Opcional) Ver reservas de un chef
    List<Reserva> findByChefUsername(String chefUsername);
}
