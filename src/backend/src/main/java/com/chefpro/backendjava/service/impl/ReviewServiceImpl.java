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

  public ReviewServiceImpl(
    ReviewRepository reviewRepository,
    ReservaRepository reservaRepository,
    CustomUserRepository userRepository,
    DinerRepository dinerRepository,
    ChefRepository chefRepository
  ) {
    this.reviewRepository = reviewRepository;
    this.reservaRepository = reservaRepository;
    this.userRepository = userRepository;
    this.dinerRepository = dinerRepository;
    this.chefRepository = chefRepository;
  }

  @Override
  @Transactional
  public void createReview(ReviewCReqDto dto, Authentication authentication) {

    // 1. Validar campos obligatorios
    if (dto.getChefId() == null) {
      throw new IllegalArgumentException("chefId is required");
    }
    if (dto.getReservationDate() == null) {
      throw new IllegalArgumentException("reservationDate is required");
    }
    if (dto.getScore() == null) {
      throw new IllegalArgumentException("score is required");
    }
    if (dto.getScore() < 1 || dto.getScore() > 5) {
      throw new IllegalArgumentException("score must be between 1 and 5");
    }

    // 2. Obtener el usuario autenticado y verificar que es un Diner
    UserLogin authUser = userRepository
      .findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));

    Diner diner = dinerRepository.findById(authUser.getId())
      .orElseThrow(() -> new RuntimeException("Only diners can submit reviews"));

    // 3. Obtener el Chef
    Chef chef = chefRepository.findById(dto.getChefId())
      .orElseThrow(() -> new RuntimeException("Chef not found"));

    // 4. Buscar la reserva por clave compuesta (chefId + date)
    // Sirve para validar que existió una relación real entre este diner y este chef
    Reservation.ReservationId reservationId = new Reservation.ReservationId(dto.getChefId(), dto.getReservationDate());
    Reservation reservation = reservaRepository.findById(reservationId)
      .orElseThrow(() -> new RuntimeException("Reservation not found"));

    // 5. Verificar que la reserva pertenece al diner autenticado
    if (!reservation.getDiner().getId().equals(diner.getId())) {
      throw new RuntimeException("This reservation does not belong to you");
    }

    // 6. Verificar que la reserva ya ocurrió (fecha anterior a hoy)
    if (!reservation.getDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("You can only review a reservation that has already taken place");
    }

    // 7. Verificar que la reserva fue CONFIRMED o COMPLETED
    if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED
      && reservation.getStatus() != Reservation.ReservationStatus.COMPLETED) {
      throw new IllegalArgumentException("You can only review a confirmed or completed reservation");
    }

    // 8. Verificar que este diner no ha reseñado ya a este chef
    boolean alreadyReviewed = reviewRepository.existsByReviewedUser_IdAndReviewerUser_Id(
      chef.getUser().getId(),
      diner.getId()
    );
    if (alreadyReviewed) {
      throw new IllegalArgumentException("You have already submitted a review for this chef");
    }

    // 9. Crear y persistir la review usando los user_ID de ambos, como indica la tabla
    Review review = Review.builder()
      .reviewedUser(chef.getUser())
      .reviewerUser(diner.getUser())
      .score(dto.getScore())
      .comment(dto.getComment())
      .build();

    reviewRepository.save(review);
  }
}
