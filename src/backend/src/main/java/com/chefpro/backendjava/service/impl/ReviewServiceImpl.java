package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.ReviewCReqDto;
import com.chefpro.backendjava.common.object.entity.*;
import com.chefpro.backendjava.repository.*;
import com.chefpro.backendjava.service.ReviewService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component("reviewService")
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReservaRepository reservaRepository;
  private final CustomUserRepository userRepository;
  private final DinerRepository dinerRepository;
  private final ChefRepository chefRepository;

  public ReviewServiceImpl(ReviewRepository reviewRepository,
                           ReservaRepository reservaRepository,
                           CustomUserRepository userRepository,
                           DinerRepository dinerRepository,
                           ChefRepository chefRepository) {
    this.reviewRepository = reviewRepository;
    this.reservaRepository = reservaRepository;
    this.userRepository = userRepository;
    this.dinerRepository = dinerRepository;
    this.chefRepository = chefRepository;
  }

  @Override
  @Transactional
  public void createReview(ReviewCReqDto dto, Authentication authentication) {
    if (dto.getChefId() == null)         throw new IllegalArgumentException("chefId is required");
    if (dto.getReservationDate() == null) throw new IllegalArgumentException("reservationDate is required");
    if (dto.getScore() == null)          throw new IllegalArgumentException("score is required");
    if (dto.getScore() < 1 || dto.getScore() > 5) throw new IllegalArgumentException("score must be between 1 and 5");

    UserLogin authUser = userRepository.findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    Chef chef = chefRepository.findById(dto.getChefId())
      .orElseThrow(() -> new RuntimeException("Chef not found"));

    Reservation reservation = reservaRepository
      .findById(new Reservation.ReservationId(dto.getChefId(), dto.getReservationDate()))
      .orElseThrow(() -> new RuntimeException("Reservation not found"));

    // Solo se puede dejar review si la reserva está confirmada o completada y ya ha pasado
    if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED
      && reservation.getStatus() != Reservation.ReservationStatus.COMPLETED) {
      throw new IllegalArgumentException("You can only review a confirmed or completed reservation");
    }
    if (!reservation.getDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("You can only review a reservation that has already taken place");
    }

    // Determinar a quién se está puntuando
    UserLogin reviewedUser;
    UserLogin reviewerUser = authUser;
    Long dinerId = reservation.getDiner().getUser().getId();
    Long chefUserId = chef.getUser().getId();

    if (authUser.getId().equals(dinerId)) {
      // El comensal puntúa al chef
      reviewedUser = chef.getUser();
    } else if (authUser.getId().equals(chefUserId)) {
      // El chef puntúa al comensal
      reviewedUser = reservation.getDiner().getUser();
    } else {
      throw new RuntimeException("You are not part of this reservation");
    }

    // Validar que no exista ya una review de este reviewer a este reviewed para esta reserva
    boolean alreadyReviewed = reviewRepository.existsByReviewedUserAndReviewerUserAndReservation(
      reviewedUser.getId(),
      reviewerUser.getId(),
      reservation.getDate(),
      chefUserId,
      dinerId
    );
    if (alreadyReviewed) {
      throw new IllegalArgumentException("You have already submitted a review for this user in this reservation");
    }

    reviewRepository.save(Review.builder()
      .reviewedUser(reviewedUser)
      .reviewerUser(reviewerUser)
      .score(dto.getScore())
      .comment(dto.getComment())
      .date(reservation.getDate())
      .build());
  }
}
