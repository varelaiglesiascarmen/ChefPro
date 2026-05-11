package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.ReservationDTO;
import com.chefpro.backendjava.common.object.dto.ReservationsCReqDto;
import com.chefpro.backendjava.common.object.dto.ReservationsUReqDto;
import com.chefpro.backendjava.common.object.entity.*;
import com.chefpro.backendjava.mapper.ReservationMapper;
import com.chefpro.backendjava.repository.*;
import com.chefpro.backendjava.service.ReservationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component("reservationService")
public class ReservationServiceImpl implements ReservationService {

  private final ReservaRepository reservaRepository;
  private final MenuRepository menuRepository;
  private final ReservationMapper reservationMapper;
  private final CustomUserRepository userRepository;
  private final ChefRepository chefRepository;
  private final DinerRepository dinerRepository;

  public ReservationServiceImpl(ReservaRepository reservaRepository,
                                MenuRepository menuRepository,
                                ReservationMapper reservationMapper,
                                CustomUserRepository userRepository,
                                ChefRepository chefRepository,
                                DinerRepository dinerRepository) {
    this.reservaRepository = reservaRepository;
    this.menuRepository = menuRepository;
    this.reservationMapper = reservationMapper;
    this.userRepository = userRepository;
    this.chefRepository = chefRepository;
    this.dinerRepository = dinerRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReservationDTO> listByChef(Authentication authentication) {
    Chef chef = chefRepository.findByUser_Username(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Chef not found"));

    return reservaRepository.findByChefId(chef.getId()).stream()
      .map(reservationMapper::toDto)
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReservationDTO> listByClient(Authentication authentication) {
    UserLogin user = userRepository.findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    Diner diner = dinerRepository.findById(user.getId())
      .orElseThrow(() -> new RuntimeException("Diner not found"));

    return reservaRepository.findByDinerId(diner.getId()).stream()
      .map(reservationMapper::toDto)
      .toList();
  }

  @Override
  @Transactional
  public void createReservations(ReservationsCReqDto dto, Authentication authentication) {
    if (dto.getChefId() == null)                                        throw new IllegalArgumentException("chefId is required");
    if (dto.getDate() == null)                                          throw new IllegalArgumentException("date is required");
    if (dto.getMenuId() == null)                                        throw new IllegalArgumentException("menuId is required");
    if (dto.getNumberOfDiners() == null || dto.getNumberOfDiners() <= 0) throw new IllegalArgumentException("numberOfDiners must be greater than 0");

    UserLogin user = userRepository.findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    Diner diner = dinerRepository.findById(user.getId())
      .orElseGet(() -> {
        Diner newDiner = new Diner();
        newDiner.setId(user.getId());
        newDiner.setUser(user);
        newDiner.setAddress(dto.getAddress());
        return dinerRepository.save(newDiner);
      });

    Chef chef = chefRepository.findById(dto.getChefId())
      .orElseThrow(() -> new RuntimeException("Chef not found"));

    Menu menu = menuRepository.findById(dto.getMenuId())
      .orElseThrow(() -> new RuntimeException("Menu not found"));

    if (!menu.getChef().getId().equals(chef.getId())) {
      throw new IllegalArgumentException("This menu does not belong to the specified chef");
    }
    if (menu.getMinNumberDiners() != null && dto.getNumberOfDiners() < menu.getMinNumberDiners()) {
      throw new IllegalArgumentException("Number of diners is below minimum required: " + menu.getMinNumberDiners());
    }
    if (menu.getMaxNumberDiners() != null && dto.getNumberOfDiners() > menu.getMaxNumberDiners()) {
      throw new IllegalArgumentException("Number of diners exceeds maximum allowed: " + menu.getMaxNumberDiners());
    }

    Reservation.ReservationId reservationId = new Reservation.ReservationId(chef.getId(), dto.getDate());
    if (reservaRepository.existsById(reservationId)) {
      throw new IllegalArgumentException("This chef already has a reservation for this date");
    }

    reservaRepository.save(reservationMapper.toEntity(dto, chef, diner, menu));
  }

  @Override
  @Transactional
  public void deleteReservation(Authentication authentication, Long chefId, LocalDate date) {
    UserLogin authUser = userRepository.findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    Reservation reservation = reservaRepository.findById(new Reservation.ReservationId(chefId, date))
      .orElseThrow(() -> new RuntimeException("Reservation not found"));

    boolean isDiner = reservation.getDiner().getId().equals(authUser.getId());
    boolean isChef  = reservation.getChef().getId().equals(authUser.getId());

    if (!isDiner && !isChef) {
      throw new RuntimeException("Not allowed to delete this reservation");
    }

    reservaRepository.deleteById(new Reservation.ReservationId(chefId, date));
  }

  @Override
  @Transactional
  public ReservationDTO updateReservationStatus(Authentication authentication, ReservationsUReqDto uReq) {
    if (uReq.getChefId() == null || uReq.getDate() == null) {
      throw new IllegalArgumentException("chefId and date are required");
    }
    if (uReq.getStatus() == null) {
      throw new IllegalArgumentException("status is required");
    }

    UserLogin authUser = userRepository.findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    Reservation reservation = reservaRepository.findById(new Reservation.ReservationId(uReq.getChefId(), uReq.getDate()))
      .orElseThrow(() -> new RuntimeException("Reservation not found"));

    boolean isDiner = reservation.getDiner().getId().equals(authUser.getId());
    if (isDiner) {
      if (uReq.getStatus() != Reservation.ReservationStatus.CANCELLED) {
        throw new IllegalArgumentException("Diners can only cancel reservations");
      }
      reservation.setStatus(uReq.getStatus());
      return reservationMapper.toDto(reservaRepository.save(reservation));
    }

    Chef chef = chefRepository.findById(authUser.getId())
      .orElseThrow(() -> new RuntimeException("Only the chef or diner of this reservation can update its status"));

    if (!reservation.getChef().getId().equals(chef.getId())) {
      throw new RuntimeException("You can only update your own reservations");
    }

    reservation.setStatus(uReq.getStatus());
    return reservationMapper.toDto(reservaRepository.save(reservation));
  }
}
