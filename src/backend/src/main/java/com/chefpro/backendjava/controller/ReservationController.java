package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.object.dto.ReservationsCReqDto;
import com.chefpro.backendjava.common.object.dto.ReservationDTO;
import com.chefpro.backendjava.common.object.dto.ReservationsUReqDto;
import com.chefpro.backendjava.service.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

  private final ReservaService reservaService;

  public ReservationController(ReservaService reservaService) {
    this.reservaService = reservaService;
  }


  @GetMapping("/chef")
  public List<ReservationDTO> getReservasDelChef(Authentication authentication) {


    return reservaService.listByChef(authentication);
  }

  @GetMapping("/comensal")
  public List<ReservationDTO> getReservasDelComensal(Authentication authentication) {


    return reservaService.listByClient(authentication);
  }


  @PostMapping("/reservas")
  public ResponseEntity<ReservationDTO> crearReservas(@RequestBody ReservationsCReqDto reservationsDto, Authentication authentication) {


    reservaService.createReservations(reservationsDto, authentication);
    return ResponseEntity.status(200).build();
  }

  @DeleteMapping("/reservas")
  public ResponseEntity<ReservationDTO> deleteReserva(Authentication authentication, @RequestBody Long idReserva) {

    reservaService.deleteReservation (authentication, idReserva);
    return ResponseEntity.status(200).build();
  }

  @PatchMapping("/reservas")
  public ReservationDTO patchReservas(Authentication authentication, @RequestBody ReservationsUReqDto ReservaUpdateDto) {

    return reservaService.updateReservations(authentication, ReservaUpdateDto);
  }

}
