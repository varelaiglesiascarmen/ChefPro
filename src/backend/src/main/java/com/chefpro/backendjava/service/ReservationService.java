package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

  List<ReservationDTO> listByChef(Authentication authentication);

  List<ReservationDTO> listByClient(Authentication authentication);

  void createReservations(ReservationsCReqDto dto, Authentication authentication);

  void deleteReservation(Authentication authentication, Long chefId, LocalDate date);

  ReservationDTO updateReservationStatus(Authentication authentication, ReservationsUReqDto uReq);
}
