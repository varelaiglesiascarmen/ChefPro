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

    List<Reservation> reservations = reservaRepository.findByChefId(chef.getId());

    return reservations.stream()
      .map(reservationMapper::toDto)
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReservationDTO> listByClient(Authentication authentication) {

    UserLogin user = userRepository
      .findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    Diner diner = dinerRepository.findById(user.getId())
      .orElseThrow(() -> new RuntimeException("Diner not found"));

    List<Reservation> reservations = reservaRepository.findByDinerId(diner.getId());

    return reservations.stream()
      .map(reservationMapper::toDto)
      .toList();
  }

  @Override
  @Transactional
  public void createReservations(ReservationsCReqDto dto, Authentication authentication) {

    // 1. Obtener el usuario autenticado
    UserLogin user = userRepository
      .findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    // 2. Buscar o crear el Diner
    Diner diner = dinerRepository.findById(user.getId())
      .orElseGet(() -> {
        // Si no existe, crear uno nuevo
        Diner newDiner = new Diner();
        newDiner.setId(user.getId());
        newDiner.setUser(user);
        newDiner.setAddress(dto.getAddress()); // Usar la dirección de la reserva como dirección por defecto
        return dinerRepository.save(newDiner);
      });

    // 3. Validaciones
    if (dto.getChefId() == null) {
      throw new IllegalArgumentException("chefId is required");
    }
    if (dto.getDate() == null) {
      throw new IllegalArgumentException("date is required");
    }
    if (dto.getMenuId() == null) {
      throw new IllegalArgumentException("menuId is required");
    }
    if (dto.getNumberOfDiners() == null || dto.getNumberOfDiners() <= 0) {
      throw new IllegalArgumentException("numberOfDiners must be greater than 0");
    }

    // 4. Obtener el Chef
    Chef chef = chefRepository.findById(dto.getChefId())
      .orElseThrow(() -> new RuntimeException("Chef not found"));

    // 5. Obtener el Menu y verificar que pertenece al chef
    Menu menu = menuRepository.findById(dto.getMenuId())
      .orElseThrow(() -> new RuntimeException("Menu not found"));

    if (!menu.getChef().getId().equals(chef.getId())) {
      throw new IllegalArgumentException("This menu does not belong to the specified chef");
    }

    // 6. Verificar que el número de comensales está dentro del rango del menú
    if (menu.getMinNumberDiners() != null && dto.getNumberOfDiners() < menu.getMinNumberDiners()) {
      throw new IllegalArgumentException("Number of diners is below minimum required: " + menu.getMinNumberDiners());
    }
    if (menu.getMaxNumberDiners() != null && dto.getNumberOfDiners() > menu.getMaxNumberDiners()) {
      throw new IllegalArgumentException("Number of diners exceeds maximum allowed: " + menu.getMaxNumberDiners());
    }

    // 7. Verificar que no existe ya una reserva para ese chef en esa fecha
    Reservation.ReservationId reservationId = new Reservation.ReservationId(chef.getId(), dto.getDate());
    if (reservaRepository.existsById(reservationId)) {
      throw new IllegalArgumentException("This chef already has a reservation for this date");
    }

    // 8. Crear la reserva
    Reservation reservation = reservationMapper.toEntity(dto, chef, diner, menu);
    reservaRepository.save(reservation);
  }

  @Override
  @Transactional
  public void deleteReservation(Authentication authentication, Long chefId, LocalDate date) {

    UserLogin authUser = userRepository
      .findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    Reservation.ReservationId reservationId = new Reservation.ReservationId(chefId, date);
    Reservation reservation = reservaRepository
      .findById(reservationId)
      .orElseThrow(() -> new RuntimeException("Reservation not found"));

    boolean isDiner = reservation.getDiner().getId().equals(authUser.getId());
    boolean isChef = reservation.getChef().getId().equals(authUser.getId());

    if (!isDiner && !isChef) {
      throw new RuntimeException("Not allowed to delete this reservation");
    }

    reservaRepository.deleteById(reservationId);
  }

  @Override
  @Transactional
  public ReservationDTO updateReservationStatus(Authentication authentication, ReservationsUReqDto uReq) {

    if (uReq.getChefId() == null || uReq.getDate() == null) {
      throw new IllegalArgumentException("chefId and date are required to identify the reservation");
    }

    if (uReq.getStatus() == null) {
      throw new IllegalArgumentException("status is required");
    }

    UserLogin authUser = userRepository
      .findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    Reservation.ReservationId reservationId = new Reservation.ReservationId(uReq.getChefId(), uReq.getDate());
    Reservation reservation = reservaRepository
      .findById(reservationId)
      .orElseThrow(() -> new RuntimeException("Reservation not found"));

    // Diners can only cancel their own reservations
    boolean isDiner = reservation.getDiner().getId().equals(authUser.getId());
    if (isDiner) {
      if (uReq.getStatus() != Reservation.ReservationStatus.CANCELLED) {
        throw new IllegalArgumentException("Diners can only cancel reservations");
      }
      reservation.setStatus(uReq.getStatus());
      Reservation saved = reservaRepository.save(reservation);
      return reservationMapper.toDto(saved);
    }

    // Chefs can accept, reject, or mark as completed
    Chef chef = chefRepository.findById(authUser.getId())
      .orElseThrow(() -> new RuntimeException("Only the chef or diner of this reservation can update its status"));

    if (!reservation.getChef().getId().equals(chef.getId())) {
      throw new RuntimeException("You can only update your own reservations");
    }

    reservation.setStatus(uReq.getStatus());

    Reservation saved = reservaRepository.save(reservation);
    return reservationMapper.toDto(saved);
  }
}
