package com.chefpro.backendjava.common.object.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diner {

  @Id
  @Column(name = "user_ID")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "user_ID")
  private UserLogin user;

  @Column(name = "address", length = 255)
  private String address;

  // Relación con reservations
  @OneToMany(mappedBy = "diner", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Reservation> reservations = new ArrayList<>();
}
