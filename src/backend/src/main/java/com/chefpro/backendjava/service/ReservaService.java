package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.*;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ReservaService {

        List<ReservationDTO> listByChef(Authentication authentication);

        List<ReservationDTO> listByClient(Authentication authentication);

        void  createReservations (ReservationsCReqDto dto, Authentication authentication);

        void deleteReservation(Authentication authentication, Long idReservation);

        ReservationDTO updateReservations(Authentication authentication, ReservationsUReqDto uReq);
    }
