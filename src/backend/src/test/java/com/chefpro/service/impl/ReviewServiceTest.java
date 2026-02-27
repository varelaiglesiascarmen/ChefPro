package com.chefpro.service.impl;

import com.chefpro.backendjava.common.object.dto.ReviewCReqDto;
import com.chefpro.backendjava.common.object.entity.*;
import com.chefpro.backendjava.repository.*;
import com.chefpro.backendjava.service.ReviewService;
import com.chefpro.backendjava.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    private ReviewService reviewService;

    private ReviewRepository reviewRepository;
    private ReservaRepository reservaRepository;
    private CustomUserRepository userRepository;
    private DinerRepository dinerRepository;
    private ChefRepository chefRepository;
    private Authentication authentication;

    private UserLogin dinerUser;
    private UserLogin chefUser;
    private Diner diner;
    private Chef chef;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reviewRepository  = mock(ReviewRepository.class);
        reservaRepository = mock(ReservaRepository.class);
        userRepository    = mock(CustomUserRepository.class);
        dinerRepository   = mock(DinerRepository.class);
        chefRepository    = mock(ChefRepository.class);
        authentication    = mock(Authentication.class);

        reviewService = new ReviewServiceImpl(
            reviewRepository, reservaRepository, userRepository, dinerRepository, chefRepository
        );

        dinerUser = mock(UserLogin.class);
        when(dinerUser.getId()).thenReturn(2L);

        chefUser = mock(UserLogin.class);
        when(chefUser.getId()).thenReturn(1L);

        diner = mock(Diner.class);
        when(diner.getId()).thenReturn(2L);
        when(diner.getUser()).thenReturn(dinerUser);

        chef = mock(Chef.class);
        when(chef.getId()).thenReturn(1L);
        when(chef.getUser()).thenReturn(chefUser);

        reservation = mock(Reservation.class);
        when(reservation.getDiner()).thenReturn(diner);
        when(reservation.getChef()).thenReturn(chef);
        when(reservation.getDate()).thenReturn(LocalDate.now().minusDays(1)); // ya pasó
        when(reservation.getStatus()).thenReturn(Reservation.ReservationStatus.CONFIRMED);

        when(authentication.getName()).thenReturn("diner@example.com");
        when(userRepository.findByUsername("diner@example.com")).thenReturn(Optional.of(dinerUser));
        when(dinerRepository.findById(2L)).thenReturn(Optional.of(diner));
        when(chefRepository.findById(1L)).thenReturn(Optional.of(chef));
        when(reservaRepository.findById(any(Reservation.ReservationId.class)))
            .thenReturn(Optional.of(reservation));
        when(reviewRepository.existsByReviewedUser_IdAndReviewerUser_Id(1L, 2L)).thenReturn(false);
    }

    private ReviewCReqDto validDto() {
        ReviewCReqDto dto = mock(ReviewCReqDto.class);
        when(dto.getChefId()).thenReturn(1L);
        when(dto.getReservationDate()).thenReturn(LocalDate.now().minusDays(1));
        when(dto.getScore()).thenReturn(5);
        when(dto.getComment()).thenReturn("Excelente chef");
        return dto;
    }

    // ─── createReview ────────────────────────────────────────────────────────

    @Test
    void createReview_success_savesReview() {
        reviewService.createReview(validDto(), authentication);

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_nullChefId_throwsIllegalArgumentException() {
        ReviewCReqDto dto = mock(ReviewCReqDto.class);
        when(dto.getChefId()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
            () -> reviewService.createReview(dto, authentication));
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createReview_nullScore_throwsIllegalArgumentException() {
        ReviewCReqDto dto = mock(ReviewCReqDto.class);
        when(dto.getChefId()).thenReturn(1L);
        when(dto.getReservationDate()).thenReturn(LocalDate.now().minusDays(1));
        when(dto.getScore()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
            () -> reviewService.createReview(dto, authentication));
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createReview_scoreOutOfRange_throwsIllegalArgumentException() {
        ReviewCReqDto dto = mock(ReviewCReqDto.class);
        when(dto.getChefId()).thenReturn(1L);
        when(dto.getReservationDate()).thenReturn(LocalDate.now().minusDays(1));
        when(dto.getScore()).thenReturn(6); // fuera de rango 1-5

        assertThrows(IllegalArgumentException.class,
            () -> reviewService.createReview(dto, authentication));
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createReview_reservationInFuture_throwsIllegalArgumentException() {
        when(reservation.getDate()).thenReturn(LocalDate.now().plusDays(1)); // fecha futura

        assertThrows(IllegalArgumentException.class,
            () -> reviewService.createReview(validDto(), authentication));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_reservationNotConfirmed_throwsIllegalArgumentException() {
        when(reservation.getStatus()).thenReturn(Reservation.ReservationStatus.PENDING);

        assertThrows(IllegalArgumentException.class,
            () -> reviewService.createReview(validDto(), authentication));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_alreadyReviewed_throwsIllegalArgumentException() {
        when(reviewRepository.existsByReviewedUser_IdAndReviewerUser_Id(1L, 2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> reviewService.createReview(validDto(), authentication));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_dinerNotFound_throwsRuntimeException() {
        when(dinerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> reviewService.createReview(validDto(), authentication));
        verifyNoInteractions(reviewRepository);
    }
}
