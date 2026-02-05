package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.object.dto.ReservationsCReqDto;
import com.chefpro.backendjava.common.object.dto.ReservationDTO;
import com.chefpro.backendjava.common.object.dto.ReservationsUReqDto;
import com.chefpro.backendjava.service.ReservaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

  private final ReservaService reservaService;

  public ReservationController(ReservaService reservaService) {
    this.reservaService = reservaService;
  }

  @GetMapping("/chef")
  public ResponseEntity<List<ReservationDTO>> getReservasDelChef(Authentication authentication) {
    List<ReservationDTO> reservations = reservaService.listByChef(authentication);
    return ResponseEntity.ok(reservations);
  }

  @GetMapping("/comensal")
  public ResponseEntity<List<ReservationDTO>> getReservasDelComensal(Authentication authentication) {
    List<ReservationDTO> reservations = reservaService.listByClient(authentication);
    return ResponseEntity.ok(reservations);
  }

  @PostMapping
  public ResponseEntity<Void> crearReservas(@RequestBody ReservationsCReqDto reservationsDto, Authentication authentication) {
    reservaService.createReservations(reservationsDto, authentication);
    return ResponseEntity.status(201).build();
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteReserva(
    Authentication authentication,
    @RequestParam Long chefId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    reservaService.deleteReservation(authentication, chefId, date);
    return ResponseEntity.status(204).build();
  }

  @PatchMapping
  public ResponseEntity<ReservationDTO> patchReservas(
    Authentication authentication,
    @RequestBody ReservationsUReqDto reservaUpdateDto
  ) {
    ReservationDTO updated = reservaService.updateReservations(authentication, reservaUpdateDto);
    return ResponseEntity.ok(updated);
  }
}
