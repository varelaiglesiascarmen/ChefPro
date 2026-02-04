package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reservation, Long> {

    // Ver reservas de un comensal
    List<Reservation> findByComensalUsername(String comensalUsername);

    // (Opcional) Ver reservas de un chef
    List<Reservation> findByChefUsername(String chefUsername);

  @Query("""
   select r
   from Reservation r
   join fetch r.menu m
   where r.id = :id
     and r.comensalId = :comensalId
""")
  Optional<Reservation> findForUpdateByIdAndComensalId(Long id, Long comensalId);
}
