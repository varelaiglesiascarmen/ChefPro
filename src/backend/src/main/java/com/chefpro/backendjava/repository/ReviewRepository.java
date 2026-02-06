package com.chefpro.backendjava.repository;

import com.chefpro.backendjava.common.object.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  @Query("SELECT r FROM Review r " +
    "JOIN FETCH r.reviewerUser " +
    "WHERE r.reviewedUser.id = :userId " +
    "ORDER BY r.date DESC")
  List<Review> findByReviewedUserIdWithReviewer(@Param("userId") Long userId);

  @Query("SELECT COALESCE(AVG(r.score), 0) FROM Review r WHERE r.reviewedUser.id = :userId")
  Double findAverageScoreByReviewedUserId(@Param("userId") Long userId);

  @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewedUser.id = :userId")
  Long countByReviewedUserId(@Param("userId") Long userId);
}
