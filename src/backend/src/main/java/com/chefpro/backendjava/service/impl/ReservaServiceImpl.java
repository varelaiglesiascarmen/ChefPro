package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.ReservationDTO;
import com.chefpro.backendjava.common.object.dto.ReservationsCReqDto;
import com.chefpro.backendjava.common.object.dto.ReservationsUReqDto;
import com.chefpro.backendjava.common.object.entity.Menu;
import com.chefpro.backendjava.common.object.entity.Reservation;
import com.chefpro.backendjava.common.object.entity.UserLogin;
import com.chefpro.backendjava.mapper.ReservationMapper;
import com.chefpro.backendjava.repository.CustomUserRepository;
import com.chefpro.backendjava.repository.MenuRepository;
import com.chefpro.backendjava.repository.ReservaRepository;
import com.chefpro.backendjava.service.ReservaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component("reservaService")
public class ReservaServiceImpl implements ReservaService {

  private final ReservaRepository reservaRepository;
  private final MenuRepository menuRepository;
  private final ReservationMapper reservationMapper;
  private final CustomUserRepository userRepository;

  public ReservaServiceImpl(ReservaRepository reservaRepository,
                            MenuRepository menuRepository,
                            ReservationMapper reservationMapper,
                            CustomUserRepository userRepository) {
    this.reservaRepository = reservaRepository;
    this.menuRepository = menuRepository;
    this.reservationMapper = reservationMapper;
    this.userRepository = userRepository;
  }

  @Override
  public List<ReservationDTO> listByChef(Authentication authentication) {

    List<Reservation> reservations =
      reservaRepository.findByChefUsername(authentication.getName());

    return reservations.stream()
      .map(r -> {
        String chefUsername = userRepository.findById(r.getChefId())
          .map(UserLogin::getUsername)
          .orElse(null);
        return reservationMapper.toDto(r, chefUsername);
      })
      .toList();
  }

  @Override
  public List<ReservationDTO> listByClient(Authentication authentication) {

    List<Reservation> reservations =
      reservaRepository.findByComensalUsername(authentication.getName());

    return reservations.stream()
      .map(r -> {
        String chefUsername = userRepository.findById(r.getChefId())
          .map(UserLogin::getUsername)
          .orElse(null);
        return reservationMapper.toDto(r, chefUsername);
      })
      .toList();
  }

  @Override
  @Transactional
  public void createReservations(ReservationsCReqDto dto, Authentication authentication) {

    Optional<UserLogin> foundUser = userRepository.findByUsername(authentication.getName());

    if (foundUser.isPresent()) {

      Menu menu = menuRepository.findById(dto.getMenuId())
        .orElseThrow(() -> new RuntimeException("Menu not found: " + dto.getMenuId()));

      if (dto.getNumberOfSeats() <= 0) {
        throw new IllegalArgumentException("numberOfSeats must be > 0");
      }
      if (dto.getDate() == null) {
        throw new IllegalArgumentException("date is required");
      }
      if (dto.getLocation() == null || dto.getLocation().isBlank()) {
        throw new IllegalArgumentException("location is required");
      }

      Reservation reservation = getReservation(dto, foundUser, menu);

      reservaRepository.save(reservation);
    }
  }

  private static Reservation getReservation(ReservationsCReqDto dto, Optional<UserLogin> foundUser, Menu menu) {
    Reservation reservation = new Reservation();

    reservation.setComensalId(foundUser.get().getId());

    reservation.setChefId(menu.getChefId());

    reservation.setMenu(menu);

    reservation.setReservationDate(dto.getDate().toLocalDateTime());
    reservation.setDeliveryAddress(dto.getLocation());
    reservation.setAdditionalNotes(dto.getDescription());
    reservation.setNumberOfDiners(dto.getNumberOfSeats());
    return reservation;
  }

  @Override
  @Transactional
  public void deleteReservation(Authentication authentication, Long idReservation) {


    UserLogin authUser = userRepository.findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

    Reservation reservation = reservaRepository.findById(idReservation)
      .orElseThrow(() -> new RuntimeException("Reservation not found: " + idReservation));

    if (!authUser.getId().equals(reservation.getComensalId())) {
      throw new RuntimeException("Not allowed to delete this reservation");
    }

    reservaRepository.deleteById(idReservation);
  }

  @Override
  @Transactional
  public ReservationDTO updateReservations(Authentication authentication, ReservationsUReqDto uReq) {

    UserLogin authUser = userRepository
      .findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

    Reservation reservation = reservaRepository
      .findForUpdateByIdAndComensalId(uReq.getId(), authUser.getId())
      .orElseThrow(() -> new RuntimeException("Reservation not found"));

    Menu newMenu = null;

    if (uReq.getMenu() != null && uReq.getMenu().getId() != null) {
      Long newMenuId = Long.valueOf(uReq.getMenu().getId());

      newMenu = menuRepository.findById(newMenuId)
        .orElseThrow(() -> new RuntimeException("Menu not found"));
    }

    reservationMapper.applyUpdate(reservation, uReq, newMenu);

    Reservation saved = reservaRepository.save(reservation);

    String chefUsername = userRepository
      .findById(saved.getChefId())
      .map(UserLogin::getUsername)
      .orElse(null);

    return reservationMapper.toDto(saved, chefUsername);
  }
}
