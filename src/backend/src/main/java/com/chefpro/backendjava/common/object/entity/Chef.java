package com.chefpro.backendjava.common.object.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chefs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chef {

  @Id
  @Column(name = "user_ID")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "user_ID")
  private UserLogin user;

  @Column(name = "photo", length = 255)
  private String photo;

  @Lob
  @Column(name = "bio")
  private String bio;

  @Lob
  @Column(name = "prizes")
  private String prizes;

  // Relación con menus
  @OneToMany(mappedBy = "chef", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Menu> menus = new ArrayList<>();

  // Relación con reservations
  @OneToMany(mappedBy = "chef")
  @Builder.Default
  private List<Reservation> reservations = new ArrayList<>();
}
