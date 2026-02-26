package com.chefpro.backendjava.common.object.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "review_ID")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reviewed_user", nullable = false)
  private UserLogin reviewedUser;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reviewer_user", nullable = false)
  private UserLogin reviewerUser;

  @Column(name = "score", nullable = false)
  private Integer score;

  @Lob
  @Column(name = "comment")
  private String comment;

  @Column(name = "date")
  private LocalDate date;

  @PrePersist
  public void prePersist() {
    if (date == null) {
      date = LocalDate.now();
    }
  }
}
