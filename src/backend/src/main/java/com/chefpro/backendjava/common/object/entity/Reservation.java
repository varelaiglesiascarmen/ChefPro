package com.chefpro.backendjava.common.object.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Relación con el comensal (UserLogin)
  //@ManyToOne(optional = false)
  @JoinColumn(name = "comensal_id", nullable = false)
  private Long comensalId;

  // Relación con el chef (UserLogin)
  //@ManyToOne(optional = false)
  @JoinColumn(name = "chef_id", nullable = false)
  private Long chefId;

  // Relación con el menú reservado
  @ManyToOne(optional = false)
  @JoinColumn(name = "menu_id", nullable = false)
  private Menu menu;

  @Column(name = "reservation_date", nullable = false)
  private LocalDateTime reservationDate;

  @Column(name = "number_of_diners", nullable = false)
  private Integer numberOfDiners;

  @Column(name = "delivery_address", length = 300, nullable = false)
  private String deliveryAddress;

  // Estado: PENDIENTE, ACEPTADA, RECHAZADA, CANCELADA, COMPLETADA
  @Column(length = 20, nullable = false)
  @Builder.Default
  private String status = "PENDIENTE";

  // Alergias del comensal
  @ElementCollection
  @CollectionTable(name = "reservation_allergies", joinColumns = @JoinColumn(name = "reservation_id"))
  @Column(name = "allergy", length = 100)
  @Builder.Default
  private List<String> allergies = new ArrayList<>();

  @Column(name = "dietary_preferences", length = 500)
  private String dietaryPreferences;

  @Column(name = "additional_notes", length = 1000)
  private String additionalNotes;

  // Valoración (1-5 estrellas) después de completar
  @Column(name = "rating")
  private Integer rating;

  @Column(name = "rating_comment", length = 1000)
  private String ratingComment;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (updatedAt == null) {
      updatedAt = Instant.now();
    }
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = Instant.now();
  }
}


