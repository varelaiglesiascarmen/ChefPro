package com.chefpro.backendjava.mapper;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.common.object.entity.Menu;
import com.chefpro.backendjava.common.object.entity.Reservation;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Component
public class ReservationMapper {

  // ENTITY -> DTO
  public ReservationDTO toDto(Reservation r, String chefUsername) {
    if (r == null) return null;

    ReservationDTO dto = new ReservationDTO();
    dto.setId(r.getId() != null ? r.getId().toString() : null);

    // "name" no existe en Reservation: lo sacamos del menú
    dto.setName(r.getMenu() != null ? r.getMenu().getTitle() : null);

    dto.setDescription(r.getAdditionalNotes());
    dto.setLocation(r.getDeliveryAddress());
    dto.setCustomerId(r.getComensalId());

    if (r.getReservationDate() != null) {
      dto.setDate(
        r.getReservationDate()
          .atZone(ZoneId.systemDefault())
          .toOffsetDateTime()
      );
    }

    dto.setMenu(mapMenu(r.getMenu(), chefUsername));

    return dto;
  }

  // CREATE REQ -> ENTITY
  public Reservation toEntity(ReservationsCReqDto dto, Long comensalId, Long chefId, Menu menu) {
    if (dto == null) return null;

    return Reservation.builder()
      .comensalId(comensalId)
      .chefId(chefId)
      .menu(menu)
      .reservationDate(dto.getDate() != null ? dto.getDate().toLocalDateTime() : null)
      .deliveryAddress(dto.getLocation())
      .additionalNotes(dto.getDescription())
      .numberOfDiners(dto.getNumberOfSeats())
      .build();
  }

  // UPDATE REQ -> ENTITY
  public void applyUpdate(Reservation reservation, ReservationsUReqDto uReq, Menu newMenuOrNull) {
    if (reservation == null || uReq == null) return;

    if (uReq.getDate() != null) {
      reservation.setReservationDate(uReq.getDate().toLocalDateTime());
    }

    if (uReq.getLocation() != null && !uReq.getLocation().isBlank()) {
      reservation.setDeliveryAddress(uReq.getLocation());
    }

    if (uReq.getDescription() != null) {
      reservation.setAdditionalNotes(uReq.getDescription());
    }

    if (uReq.getNumberOfSeats() > 0) {
      reservation.setNumberOfDiners(uReq.getNumberOfSeats());
    }

    // Si decides permitir cambio de menú:
    if (newMenuOrNull != null) {
      reservation.setMenu(newMenuOrNull);

      reservation.setChefId(newMenuOrNull.getChefId());
    }
  }


  // Menu -> MenuDTO
  private MenuDTO mapMenu(Menu m, String chefUsername) {
    if (m == null) return null;

    List<PlatoDto> dishDtos = (m.getDishes() == null)
      ? List.of()
      : m.getDishes().stream()
      .map(p -> PlatoDto.builder()
        .id(p.getId())
        .title(p.getTitle())
        .description(p.getDescription())
        .build()
      )
      .toList();

    Set<String> allergens = (m.getAllergens() == null) ? Set.of() : m.getAllergens();

    return MenuDTO.builder()
      .id(m.getId() != null ? m.getId().toString() : null)
      .title(m.getTitle())
      .description(m.getDescription())
      .dishes(dishDtos)
      .allergens(allergens)
      .pricePerPerson(m.getPricePerPerson())
      .deliveryAvailable(m.isDeliveryAvailable())
      .cookAtClientHome(m.isCookAtClientHome())
      .pickupAvailable(m.isPickupAvailable())
      .chefUsername(chefUsername) // ✅ username REAL, no id
      .createdAt(m.getCreatedAt())
      .build();
  }
}
