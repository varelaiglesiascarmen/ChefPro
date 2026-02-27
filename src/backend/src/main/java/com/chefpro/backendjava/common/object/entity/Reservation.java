package com.chefpro.backendjava.common.object.entity;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(Reservation.ReservationId.class)
public class Reservation {

  @Id
  @Column(name = "chef_ID", nullable = false)
  private Long chefId;

  @Id
  @Column(name = "date", nullable = false)
  private LocalDate date;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "chef_ID", insertable = false, updatable = false)
  private Chef chef;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "diner_ID", nullable = false)
  private Diner diner;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "menu_ID", nullable = false)
  private Menu menu;

  @Column(name = "n_diners", nullable = false)
  private Integer numberOfDiners;

  @Column(name = "address", length = 255)
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private ReservationStatus status = ReservationStatus.PENDING;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (status == null) {
      status = ReservationStatus.PENDING;
    }
  }

  // Enum para el status
  public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELLED,
    COMPLETED
  }

  // Clase interna para la clave compuesta
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ReservationId implements Serializable {
    private Long chefId;
    private LocalDate date;
  }
}
