package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {


  @Query("SELECT r FROM Review r " +
    "JOIN FETCH r.reviewerUser " +
    "WHERE r.reviewedUser.id = :reviewedUserId " +
    "ORDER BY r.date DESC")
  List<Review> findByReviewedUserIdWithReviewer(@Param("reviewedUserId") Long reviewedUserId);

  // Media de puntuación (devuelve 0 si no hay reviews)
  @Query("SELECT COALESCE(AVG(r.score), 0) FROM Review r WHERE r.reviewedUser.id = :reviewedUserId")
  Double findAverageScoreByReviewedUserId(@Param("reviewedUserId") Long reviewedUserId);

  // Número total de reseñas
  @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewedUser.id = :reviewedUserId")
  Long countByReviewedUserId(@Param("reviewedUserId") Long reviewedUserId);

  // Evitar reseñas duplicadas
  boolean existsByReviewedUser_IdAndReviewerUser_Id(Long reviewedUserId, Long reviewerUserId);
}
