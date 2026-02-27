package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  // Usado en ChefProfileServiceImpl.getChefPublicProfile() para listar reseñas
  // Hace fetch del reviewer para poder acceder a nombre/apellido sin lazy load adicional
  @Query("SELECT r FROM Review r " +
    "JOIN FETCH r.reviewerUser " +
<<<<<<< HEAD
    "WHERE r.reviewedUser.id = :reviewedUserId")
  List<Review> findByReviewedUserIdWithReviewer(@Param("reviewedUserId") Long reviewedUserId);

  // Usado en ChefProfileServiceImpl para calcular la media de puntuación
  @Query("SELECT AVG(r.score) FROM Review r WHERE r.reviewedUser.id = :reviewedUserId")
=======
    "WHERE r.reviewedUser.id = :reviewedUserId " +
    "ORDER BY r.date DESC")
  List<Review> findByReviewedUserIdWithReviewer(@Param("reviewedUserId") Long reviewedUserId);

  // Usado en ChefProfileServiceImpl para calcular la media de puntuación
  @Query("SELECT COALESCE(AVG(r.score), 0) FROM Review r WHERE r.reviewedUser.id = :reviewedUserId")
>>>>>>> 92e126861fcf8bdb5428abe2ca3b3b2043c4af64
  Double findAverageScoreByReviewedUserId(@Param("reviewedUserId") Long reviewedUserId);

  // Usado en ChefProfileServiceImpl para contar el número de reseñas
  @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewedUser.id = :reviewedUserId")
  Long countByReviewedUserId(@Param("reviewedUserId") Long reviewedUserId);

  // Usado en ReviewServiceImpl para evitar reseñas duplicadas del mismo diner al mismo chef
  boolean existsByReviewedUser_IdAndReviewerUser_Id(Long reviewedUserId, Long reviewerUserId);
}
