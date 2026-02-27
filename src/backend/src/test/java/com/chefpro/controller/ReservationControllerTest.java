package com.chefpro.controller;

import com.chefpro.backendjava.common.object.dto.ReservationDTO;
import com.chefpro.backendjava.common.object.dto.ReservationsCReqDto;
import com.chefpro.backendjava.common.object.dto.ReservationsUReqDto;
import com.chefpro.backendjava.common.object.dto.ReviewCReqDto;
import com.chefpro.backendjava.controller.ReservationController;
import com.chefpro.backendjava.service.ReservationService;
import com.chefpro.backendjava.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationControllerTest {

    private ReservationService reservationService;
    private ReviewService reviewService;
    private Authentication authentication;

    private ReservationController controller;

    @BeforeEach
    void setUp() {
        reservationService = mock(ReservationService.class);
        reviewService      = mock(ReviewService.class);
        authentication     = mock(Authentication.class);

        controller = new ReservationController(reservationService, reviewService);
    }

    // ─── GET /chef ───────────────────────────────────────────────────────────

    @Test
    void getReservasDelChef_success_returns200WithList() {
        ReservationDTO reservation = mock(ReservationDTO.class);
        when(reservationService.listByChef(authentication)).thenReturn(List.of(reservation));

        ResponseEntity<List<ReservationDTO>> response = controller.getReservasDelChef(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(reservationService).listByChef(authentication);
    }

    @Test
    void getReservasDelChef_serviceThrows_propagatesException() {
        when(reservationService.listByChef(authentication)).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> controller.getReservasDelChef(authentication));
        verify(reservationService).listByChef(authentication);
    }

    // ─── GET /comensal ───────────────────────────────────────────────────────

    @Test
    void getReservasDelComensal_success_returns200WithList() {
        ReservationDTO reservation = mock(ReservationDTO.class);
        when(reservationService.listByClient(authentication)).thenReturn(List.of(reservation));

        ResponseEntity<List<ReservationDTO>> response = controller.getReservasDelComensal(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(reservationService).listByClient(authentication);
    }

    @Test
    void getReservasDelComensal_serviceThrows_propagatesException() {
        when(reservationService.listByClient(authentication)).thenThrow(new RuntimeException("Service error"));

        assertThrows(RuntimeException.class, () -> controller.getReservasDelComensal(authentication));
        verify(reservationService).listByClient(authentication);
    }

    // ─── POST / ──────────────────────────────────────────────────────────────

    @Test
    void crearReservas_success_returns201() {
        ReservationsCReqDto req = mock(ReservationsCReqDto.class);
        doNothing().when(reservationService).createReservations(req, authentication);

        ResponseEntity<Void> response = controller.crearReservas(req, authentication);

        assertEquals(201, response.getStatusCode().value());
        verify(reservationService).createReservations(req, authentication);
    }

    @Test
    void crearReservas_serviceThrows_propagatesException() {
        ReservationsCReqDto req = mock(ReservationsCReqDto.class);
        doThrow(new IllegalArgumentException("Invalid reservation"))
            .when(reservationService).createReservations(req, authentication);

        assertThrows(IllegalArgumentException.class, () -> controller.crearReservas(req, authentication));
        verify(reservationService).createReservations(req, authentication);
    }

    // ─── DELETE / ────────────────────────────────────────────────────────────

    @Test
    void deleteReserva_success_returns204() {
        LocalDate date = LocalDate.of(2025, 8, 20);
        doNothing().when(reservationService).deleteReservation(authentication, 1L, date);

        ResponseEntity<Void> response = controller.deleteReserva(authentication, 1L, date);

        assertEquals(204, response.getStatusCode().value());
        verify(reservationService).deleteReservation(authentication, 1L, date);
    }

    @Test
    void deleteReserva_serviceThrows_propagatesException() {
        LocalDate date = LocalDate.of(2025, 8, 20);
        doThrow(new NoSuchElementException("Reservation not found"))
            .when(reservationService).deleteReservation(authentication, 99L, date);

        assertThrows(NoSuchElementException.class, () -> controller.deleteReserva(authentication, 99L, date));
        verify(reservationService).deleteReservation(authentication, 99L, date);
    }

    // ─── PATCH /status ───────────────────────────────────────────────────────

    @Test
    void updateReservationStatus_success_returns200WithUpdated() {
        ReservationsUReqDto req    = mock(ReservationsUReqDto.class);
        ReservationDTO updated     = mock(ReservationDTO.class);
        when(reservationService.updateReservationStatus(authentication, req)).thenReturn(updated);

        ResponseEntity<ReservationDTO> response = controller.updateReservationStatus(authentication, req);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(updated, response.getBody());
        verify(reservationService).updateReservationStatus(authentication, req);
    }

    @Test
    void updateReservationStatus_serviceThrows_propagatesException() {
        ReservationsUReqDto req = mock(ReservationsUReqDto.class);
        when(reservationService.updateReservationStatus(authentication, req))
            .thenThrow(new RuntimeException("Status update failed"));

        assertThrows(RuntimeException.class, () -> controller.updateReservationStatus(authentication, req));
        verify(reservationService).updateReservationStatus(authentication, req);
    }

    // ─── POST /review ────────────────────────────────────────────────────────

    @Test
    void createReview_success_returns201() {
        ReviewCReqDto req = mock(ReviewCReqDto.class);
        doNothing().when(reviewService).createReview(req, authentication);

        ResponseEntity<Void> response = controller.createReview(authentication, req);

        assertEquals(201, response.getStatusCode().value());
        verify(reviewService).createReview(req, authentication);
    }

    @Test
    void createReview_serviceThrows_propagatesException() {
        ReviewCReqDto req = mock(ReviewCReqDto.class);
        doThrow(new IllegalStateException("Review not allowed"))
            .when(reviewService).createReview(req, authentication);

        assertThrows(IllegalStateException.class, () -> controller.createReview(authentication, req));
        verify(reviewService).createReview(req, authentication);
    }
}
