package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.object.dto.ReservationsCReqDto;
import com.chefpro.backendjava.common.object.dto.ReservationDTO;
import com.chefpro.backendjava.common.object.dto.ReservationsUReqDto;
import com.chefpro.backendjava.common.object.dto.ReviewCReqDto;
import com.chefpro.backendjava.service.ReservationService;
import com.chefpro.backendjava.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

  private final ReservationService reservationService;
  private final ReviewService reviewService;

  public ReservationController(ReservationService reservationService, ReviewService reviewService) {
    this.reservationService = reservationService;
    this.reviewService = reviewService;
  }

  @GetMapping("/chef")
  public ResponseEntity<List<ReservationDTO>> getReservasDelChef(Authentication authentication) {
    List<ReservationDTO> reservations = reservationService.listByChef(authentication);
    return ResponseEntity.ok(reservations);
  }

  @GetMapping("/comensal")
  public ResponseEntity<List<ReservationDTO>> getReservasDelComensal(Authentication authentication) {
    List<ReservationDTO> reservations = reservationService.listByClient(authentication);
    return ResponseEntity.ok(reservations);
  }

  @PostMapping
  public ResponseEntity<Void> createReservation(
    @RequestBody @Valid ReservationsCReqDto reservationsDto,
    Authentication authentication
  ) {
    reservationService.createReservations(reservationsDto, authentication);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteReservation(
    Authentication authentication,
    @RequestParam Long chefId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    reservationService.deleteReservation(authentication, chefId, date);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PatchMapping("/status")
  public ResponseEntity<ReservationDTO> updateReservationStatus(
    Authentication authentication,
    @RequestBody ReservationsUReqDto reservaUpdateDto
  ) {
    return ResponseEntity.ok(reservationService.updateReservationStatus(authentication, reservaUpdateDto));
  }

  @PostMapping("/review")
  public ResponseEntity<Void> createReview(
    Authentication authentication,
    @RequestBody @Valid ReviewCReqDto reviewDto
  ) {
    reviewService.createReview(reviewDto, authentication);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
