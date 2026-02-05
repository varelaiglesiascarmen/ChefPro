package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.ReservationDTO;
import com.chefpro.backendjava.common.object.dto.ReservationsCReqDto;
import com.chefpro.backendjava.common.object.dto.ReservationsUReqDto;
import com.chefpro.backendjava.common.object.entity.*;
import com.chefpro.backendjava.mapper.ReservationMapper;
import com.chefpro.backendjava.repository.*;
import com.chefpro.backendjava.service.ReservaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component("reservaService")
public class ReservaServiceImpl implements ReservaService {

  private final ReservaRepository reservaRepository;
  private final MenuRepository menuRepository;
  private final ReservationMapper reservationMapper;
  private final CustomUserRepository userRepository;
  private final ChefRepository chefRepository;
  private final DinerRepository dinerRepository;

  public ReservaServiceImpl(ReservaRepository reservaRepository,
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

    // 1. Obtener el usuario autenticado (debe ser un Diner)
    UserLogin user = userRepository
      .findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    Diner diner = dinerRepository.findById(user.getId())
      .orElseThrow(() -> new RuntimeException("Diner not found"));

    // 2. Validaciones
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

    // 3. Obtener el Chef
    Chef chef = chefRepository.findById(dto.getChefId())
      .orElseThrow(() -> new RuntimeException("Chef not found"));

    // 4. Obtener el Menu y verificar que pertenece al chef
    Menu menu = menuRepository.findById(dto.getMenuId())
      .orElseThrow(() -> new RuntimeException("Menu not found"));

    if (!menu.getChef().getId().equals(chef.getId())) {
      throw new IllegalArgumentException("This menu does not belong to the specified chef");
    }

    // 5. Verificar que el número de comensales está dentro del rango del menú
    if (menu.getMinNumberDiners() != null && dto.getNumberOfDiners() < menu.getMinNumberDiners()) {
      throw new IllegalArgumentException("Number of diners is below minimum required: " + menu.getMinNumberDiners());
    }
    if (menu.getMaxNumberDiners() != null && dto.getNumberOfDiners() > menu.getMaxNumberDiners()) {
      throw new IllegalArgumentException("Number of diners exceeds maximum allowed: " + menu.getMaxNumberDiners());
    }

    // 6. Verificar que no existe ya una reserva para ese chef en esa fecha
    Reservation.ReservationId reservationId = new Reservation.ReservationId(chef.getId(), dto.getDate());
    if (reservaRepository.existsById(reservationId)) {
      throw new IllegalArgumentException("This chef already has a reservation for this date");
    }

    // 7. Crear la reserva
    Reservation reservation = reservationMapper.toEntity(dto, chef, diner, menu);
    reservaRepository.save(reservation);
  }

  @Override
  @Transactional
  public void deleteReservation(Authentication authentication, Long chefId, java.time.LocalDate date) {

    // 1. Obtener el usuario autenticado
    UserLogin authUser = userRepository
      .findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    // 2. Buscar la reserva por clave compuesta
    Reservation.ReservationId reservationId = new Reservation.ReservationId(chefId, date);
    Reservation reservation = reservaRepository
      .findById(reservationId)
      .orElseThrow(() -> new RuntimeException("Reservation not found"));

    // 3. Verificar permisos: solo el diner que hizo la reserva o el chef pueden borrarla
    boolean isDiner = reservation.getDiner().getId().equals(authUser.getId());
    boolean isChef = reservation.getChef().getId().equals(authUser.getId());

    if (!isDiner && !isChef) {
      throw new RuntimeException("Not allowed to delete this reservation");
    }

    // 4. Eliminar la reserva
    reservaRepository.deleteById(reservationId);
  }

  @Override
  @Transactional
  public ReservationDTO updateReservations(Authentication authentication, ReservationsUReqDto uReq) {

    // 1. Validaciones
    if (uReq.getChefId() == null || uReq.getDate() == null) {
      throw new IllegalArgumentException("chefId and date are required to identify the reservation");
    }

    // 2. Obtener el usuario autenticado
    UserLogin authUser = userRepository
      .findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    // 3. Buscar la reserva por clave compuesta
    Reservation.ReservationId reservationId = new Reservation.ReservationId(uReq.getChefId(), uReq.getDate());
    Reservation reservation = reservaRepository
      .findById(reservationId)
      .orElseThrow(() -> new RuntimeException("Reservation not found"));

    // 4. Verificar permisos
    boolean isDiner = reservation.getDiner().getId().equals(authUser.getId());
    boolean isChef = reservation.getChef().getId().equals(authUser.getId());

    if (!isDiner && !isChef) {
      throw new RuntimeException("Not allowed to update this reservation");
    }

    // 5. Obtener nuevo menú si se proporciona
    Menu newMenu = null;
    if (uReq.getMenuId() != null) {
      newMenu = menuRepository.findById(uReq.getMenuId())
        .orElseThrow(() -> new RuntimeException("Menu not found"));

      // Verificar que el nuevo menú pertenece al mismo chef
      if (!newMenu.getChef().getId().equals(reservation.getChef().getId())) {
        throw new IllegalArgumentException("The new menu must belong to the same chef");
      }

      // Verificar rango de comensales si se cambia el menú
      Integer numberOfDiners = uReq.getNumberOfDiners() != null
        ? uReq.getNumberOfDiners()
        : reservation.getNumberOfDiners();

      if (newMenu.getMinNumberDiners() != null && numberOfDiners < newMenu.getMinNumberDiners()) {
        throw new IllegalArgumentException("Number of diners is below minimum required for this menu");
      }
      if (newMenu.getMaxNumberDiners() != null && numberOfDiners > newMenu.getMaxNumberDiners()) {
        throw new IllegalArgumentException("Number of diners exceeds maximum allowed for this menu");
      }
    }

    // 6. Aplicar actualización
    reservationMapper.applyUpdate(reservation, uReq, newMenu);

    // 7. Guardar y devolver
    Reservation saved = reservaRepository.save(reservation);
    return reservationMapper.toDto(saved);
  }
}
