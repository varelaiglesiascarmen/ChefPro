package com.chefpro.service.impl;

import com.chefpro.backendjava.common.object.dto.ReservationDTO;
import com.chefpro.backendjava.common.object.dto.ReservationsCReqDto;
import com.chefpro.backendjava.common.object.dto.ReservationsUReqDto;
import com.chefpro.backendjava.common.object.entity.*;
import com.chefpro.backendjava.mapper.ReservationMapper;
import com.chefpro.backendjava.repository.*;
import com.chefpro.backendjava.service.ReservationService;
import com.chefpro.backendjava.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private ReservationService reservationService;

    private ReservaRepository reservaRepository;
    private MenuRepository menuRepository;
    private ReservationMapper reservationMapper;
    private CustomUserRepository userRepository;
    private ChefRepository chefRepository;
    private DinerRepository dinerRepository;
    private Authentication authentication;

    private UserLogin userLogin;
    private Chef chef;
    private Diner diner;
    private Menu menu;

    @BeforeEach
    void setUp() {
        reservaRepository  = mock(ReservaRepository.class);
        menuRepository     = mock(MenuRepository.class);
        reservationMapper  = mock(ReservationMapper.class);
        userRepository     = mock(CustomUserRepository.class);
        chefRepository     = mock(ChefRepository.class);
        dinerRepository    = mock(DinerRepository.class);
        authentication     = mock(Authentication.class);

        reservationService = new ReservationServiceImpl(
            reservaRepository, menuRepository, reservationMapper,
            userRepository, chefRepository, dinerRepository
        );

        userLogin = mock(UserLogin.class);
        when(userLogin.getId()).thenReturn(1L);

        chef = mock(Chef.class);
        when(chef.getId()).thenReturn(1L);
        when(chef.getUser()).thenReturn(userLogin);

        diner = mock(Diner.class);
        when(diner.getId()).thenReturn(1L);
        when(diner.getUser()).thenReturn(userLogin);

        menu = mock(Menu.class);
        when(menu.getId()).thenReturn(10L);
        when(menu.getChef()).thenReturn(chef);
        when(menu.getMinNumberDiners()).thenReturn(1);
        when(menu.getMaxNumberDiners()).thenReturn(10);

        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(userLogin));
    }

    // ─── listByChef ──────────────────────────────────────────────────────────

    @Test
    void listByChef_success_returnsList() {
        Reservation reservation = mock(Reservation.class);
        ReservationDTO dto      = mock(ReservationDTO.class);

        when(chefRepository.findByUser_Username("user@example.com")).thenReturn(Optional.of(chef));
        when(reservaRepository.findByChefId(1L)).thenReturn(List.of(reservation));
        when(reservationMapper.toDto(reservation)).thenReturn(dto);

        List<ReservationDTO> result = reservationService.listByChef(authentication);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(chefRepository).findByUser_Username("user@example.com");
        verify(reservaRepository).findByChefId(1L);
    }

    @Test
    void listByChef_chefNotFound_throwsRuntimeException() {
        when(chefRepository.findByUser_Username("user@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservationService.listByChef(authentication));
        verifyNoInteractions(reservaRepository);
    }

    // ─── listByClient ────────────────────────────────────────────────────────

    @Test
    void listByClient_success_returnsList() {
        Reservation reservation = mock(Reservation.class);
        ReservationDTO dto      = mock(ReservationDTO.class);

        when(dinerRepository.findById(1L)).thenReturn(Optional.of(diner));
        when(reservaRepository.findByDinerId(1L)).thenReturn(List.of(reservation));
        when(reservationMapper.toDto(reservation)).thenReturn(dto);

        List<ReservationDTO> result = reservationService.listByClient(authentication);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservaRepository).findByDinerId(1L);
    }

    @Test
    void listByClient_dinerNotFound_throwsRuntimeException() {
        when(dinerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservationService.listByClient(authentication));
        verifyNoInteractions(reservaRepository);
    }

    // ─── createReservations ──────────────────────────────────────────────────

    @Test
    void createReservations_success_savesReservation() {
        ReservationsCReqDto dto = mock(ReservationsCReqDto.class);
        LocalDate date          = LocalDate.of(2025, 10, 20);

        when(dto.getChefId()).thenReturn(1L);
        when(dto.getDate()).thenReturn(date);
        when(dto.getMenuId()).thenReturn(10L);
        when(dto.getNumberOfDiners()).thenReturn(4);
        when(dto.getAddress()).thenReturn("Calle Falsa 123");

        when(dinerRepository.findById(1L)).thenReturn(Optional.of(diner));
        when(chefRepository.findById(1L)).thenReturn(Optional.of(chef));
        when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));
        when(reservaRepository.existsById(any(Reservation.ReservationId.class))).thenReturn(false);

        Reservation reservation = mock(Reservation.class);
        when(reservationMapper.toEntity(dto, chef, diner, menu)).thenReturn(reservation);

        reservationService.createReservations(dto, authentication);

        verify(reservaRepository).save(reservation);
    }

    @Test
    void createReservations_nullChefId_throwsIllegalArgumentException() {
        ReservationsCReqDto dto = mock(ReservationsCReqDto.class);
        when(dto.getChefId()).thenReturn(null);
        when(dinerRepository.findById(1L)).thenReturn(Optional.of(diner));

        assertThrows(IllegalArgumentException.class,
            () -> reservationService.createReservations(dto, authentication));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void createReservations_duplicateReservation_throwsIllegalArgumentException() {
        ReservationsCReqDto dto = mock(ReservationsCReqDto.class);
        LocalDate date          = LocalDate.of(2025, 10, 20);

        when(dto.getChefId()).thenReturn(1L);
        when(dto.getDate()).thenReturn(date);
        when(dto.getMenuId()).thenReturn(10L);
        when(dto.getNumberOfDiners()).thenReturn(4);
        when(dto.getAddress()).thenReturn("Calle Falsa 123");

        when(dinerRepository.findById(1L)).thenReturn(Optional.of(diner));
        when(chefRepository.findById(1L)).thenReturn(Optional.of(chef));
        when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));
        when(reservaRepository.existsById(any(Reservation.ReservationId.class))).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> reservationService.createReservations(dto, authentication));
        verify(reservaRepository, never()).save(any());
    }

    // ─── deleteReservation ───────────────────────────────────────────────────

    @Test
    void deleteReservation_asDiner_deletesReservation() {
        LocalDate date      = LocalDate.of(2025, 10, 20);
        Reservation res     = mock(Reservation.class);
        when(res.getDiner()).thenReturn(diner);
        when(res.getChef()).thenReturn(chef);

        when(reservaRepository.findById(any(Reservation.ReservationId.class)))
            .thenReturn(Optional.of(res));

        reservationService.deleteReservation(authentication, 1L, date);

        verify(reservaRepository).deleteById(any(Reservation.ReservationId.class));
    }

    @Test
    void deleteReservation_reservationNotFound_throwsRuntimeException() {
        LocalDate date = LocalDate.of(2025, 10, 20);
        when(reservaRepository.findById(any(Reservation.ReservationId.class)))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> reservationService.deleteReservation(authentication, 1L, date));
        verify(reservaRepository, never()).deleteById(any());
    }

    // ─── updateReservationStatus ─────────────────────────────────────────────

    @Test
    void updateReservationStatus_success_returnsUpdatedDto() {
        ReservationsUReqDto uReq = mock(ReservationsUReqDto.class);
        LocalDate date           = LocalDate.of(2025, 10, 20);

        when(uReq.getChefId()).thenReturn(1L);
        when(uReq.getDate()).thenReturn(date);
        when(uReq.getStatus()).thenReturn(Reservation.ReservationStatus.CONFIRMED);

        when(chefRepository.findById(1L)).thenReturn(Optional.of(chef));

        // Reservation's diner has a different ID so the service takes the chef path
        Diner otherDiner = mock(Diner.class);
        when(otherDiner.getId()).thenReturn(99L);

        Reservation reservation = mock(Reservation.class);
        when(reservation.getChef()).thenReturn(chef);
        when(reservation.getDiner()).thenReturn(otherDiner);
        when(reservaRepository.findById(any(Reservation.ReservationId.class)))
            .thenReturn(Optional.of(reservation));

        ReservationDTO dto = mock(ReservationDTO.class);
        when(reservaRepository.save(reservation)).thenReturn(reservation);
        when(reservationMapper.toDto(reservation)).thenReturn(dto);

        ReservationDTO result = reservationService.updateReservationStatus(authentication, uReq);

        assertNotNull(result);
        verify(reservation).setStatus(Reservation.ReservationStatus.CONFIRMED);
        verify(reservaRepository).save(reservation);
    }

    @Test
    void updateReservationStatus_nullChefId_throwsIllegalArgumentException() {
        ReservationsUReqDto uReq = mock(ReservationsUReqDto.class);
        when(uReq.getChefId()).thenReturn(null);
        when(uReq.getDate()).thenReturn(LocalDate.now());

        assertThrows(IllegalArgumentException.class,
            () -> reservationService.updateReservationStatus(authentication, uReq));
        verifyNoInteractions(reservaRepository);
    }
}
