package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.object.dto.ReservationsCReqDto;
import com.chefpro.backendjava.common.object.dto.ReservationDTO;
import com.chefpro.backendjava.common.object.dto.ReservationsUReqDto;
import com.chefpro.backendjava.common.object.dto.ReviewCReqDto;
import com.chefpro.backendjava.service.ReservationService;
import com.chefpro.backendjava.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Reservas", description = "Creación, consulta y gestión de reservas y reseñas")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

  private final ReservationService reservationService;
  private final ReviewService reviewService;

  public ReservationController(ReservationService reservationService, ReviewService reviewService) {
    this.reservationService = reservationService;
    this.reviewService = reviewService;
  }

  @Operation(summary = "Listar reservas del chef", description = "Devuelve todas las reservas recibidas por el chef autenticado con su estado actual.")
  @GetMapping("/chef")
  public ResponseEntity<List<ReservationDTO>> getReservasDelChef(Authentication authentication) {
    List<ReservationDTO> reservations = reservationService.listByChef(authentication);
    return ResponseEntity.ok(reservations);
  }

  @Operation(summary = "Listar reservas del comensal", description = "Devuelve todas las reservas realizadas por el comensal autenticado con su estado actual.")
  @GetMapping("/comensal")
  public ResponseEntity<List<ReservationDTO>> getReservasDelComensal(Authentication authentication) {
    List<ReservationDTO> reservations = reservationService.listByClient(authentication);
    return ResponseEntity.ok(reservations);
  }

  @Operation(summary = "Crear reserva", description = "El comensal solicita una reserva a un chef para una fecha, menú y número de comensales concretos. Solo un comensal puede realizarla.")
  @PostMapping
  public ResponseEntity<Void> createReservation(
    @RequestBody @Valid ReservationsCReqDto reservationsDto,
    Authentication authentication
  ) {
    reservationService.createReservations(reservationsDto, authentication);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(summary = "Eliminar reserva", description = "Elimina una reserva identificada por chefId y fecha. Solo puede hacerlo el comensal o el chef implicado.")
  @DeleteMapping
  public ResponseEntity<Void> deleteReservation(
    Authentication authentication,
    @RequestParam Long chefId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    reservationService.deleteReservation(authentication, chefId, date);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Operation(summary = "Actualizar estado de reserva", description = "Cambia el estado de una reserva. El chef puede confirmarla o completarla; el comensal solo puede cancelarla.")
  @PatchMapping("/status")
  public ResponseEntity<ReservationDTO> updateReservationStatus(
    Authentication authentication,
    @RequestBody ReservationsUReqDto reservaUpdateDto
  ) {
    return ResponseEntity.ok(reservationService.updateReservationStatus(authentication, reservaUpdateDto));
  }

  @Operation(summary = "Escribir reseña", description = "El comensal publica una reseña (puntuación 1-5 y comentario opcional) sobre un chef tras una reserva confirmada o completada. Solo se permite una reseña por chef.")
  @PostMapping("/review")
  public ResponseEntity<Void> createReview(
    Authentication authentication,
    @RequestBody @Valid ReviewCReqDto reviewDto
  ) {
    reviewService.createReview(reviewDto, authentication);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
