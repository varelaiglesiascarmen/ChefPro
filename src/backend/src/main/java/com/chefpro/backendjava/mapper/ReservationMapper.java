package com.chefpro.backendjava.mapper;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.common.object.entity.Diner;
import com.chefpro.backendjava.common.object.entity.Menu;
import com.chefpro.backendjava.common.object.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

  // ENTITY -> DTO
  public ReservationDTO toDto(Reservation r) {
    if (r == null) return null;

    // Obtener nombres para información adicional
    String chefName = r.getChef() != null && r.getChef().getUser() != null
      ? r.getChef().getUser().getName() + " " + r.getChef().getUser().getLastname()
      : null;

    String dinerName = r.getDiner() != null && r.getDiner().getUser() != null
      ? r.getDiner().getUser().getName() + " " + r.getDiner().getUser().getLastname()
      : null;

    String menuTitle = r.getMenu() != null ? r.getMenu().getTitle() : null;

    return ReservationDTO.builder()
      .chefId(r.getChefId())
      .date(r.getDate())
      .dinerId(r.getDiner() != null ? r.getDiner().getId() : null)
      .menuId(r.getMenu() != null ? r.getMenu().getId() : null)
      .numberOfDiners(r.getNumberOfDiners())
      .address(r.getAddress())
      .status(r.getStatus())
      .chefName(chefName)
      .dinerName(dinerName)
      .menuTitle(menuTitle)
      .build();
  }

  // DTO CREATE -> ENTITY
  public Reservation toEntity(ReservationsCReqDto dto, Chef chef, Diner diner, Menu menu) {
    if (dto == null) return null;

    return Reservation.builder()
      .chefId(chef != null ? chef.getId() : null)
      .date(dto.getDate())
      .chef(chef)
      .diner(diner)
      .menu(menu)
      .numberOfDiners(dto.getNumberOfDiners())
      .address(dto.getAddress())
      .status(Reservation.ReservationStatus.PENDING)
      .build();
  }

  // UPDATE REQ -> ENTITY
  public void applyUpdate(Reservation reservation, ReservationsUReqDto uReq, Menu newMenuOrNull) {
    if (reservation == null || uReq == null) return;

    // Actualizar número de comensales
    if (uReq.getNumberOfDiners() != null && uReq.getNumberOfDiners() > 0) {
      reservation.setNumberOfDiners(uReq.getNumberOfDiners());
    }

    // Actualizar dirección
    if (uReq.getAddress() != null && !uReq.getAddress().isBlank()) {
      reservation.setAddress(uReq.getAddress());
    }

    // Actualizar estado
    if (uReq.getStatus() != null) {
      reservation.setStatus(uReq.getStatus());
    }

    // Actualizar menú si se proporciona uno nuevo
    if (newMenuOrNull != null) {
      reservation.setMenu(newMenuOrNull);
    }
  }
}
