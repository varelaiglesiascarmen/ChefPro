package com.chefpro.backendjava.common.object.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

  @Id
  @Column(length = 50)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 150, nullable = false)
  private String title;

  @Column(length = 300)
  private String description;

  // Many menus can include many dishes
  @ManyToMany
  @JoinTable(
    name = "menu_dish",
    joinColumns = @JoinColumn(name = "menu_id"),
    inverseJoinColumns = @JoinColumn(name = "dish_id")
  )
  @Builder.Default
  private List<Plato> dishes = new java.util.ArrayList<>();

  // Allergens stored as a separate table (recommended vs comma-separated string)
  @ElementCollection
  @CollectionTable(name = "menu_allergen", joinColumns = @JoinColumn(name = "menu_id"))
  @Column(name = "allergen", length = 100, nullable = false)
  @Builder.Default
  private Set<String> allergens = new LinkedHashSet<>();

  @Column(name = "price_per_person", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerPerson;

  @Column(name = "is_delivery_available", nullable = false)
  private boolean deliveryAvailable;

  @Column(name = "can_cook_at_client_home", nullable = false)
  private boolean cookAtClientHome;

  @Column(name = "is_pickup_available", nullable = false)
  private boolean pickupAvailable;

  //@ManyToOne(optional = false)
  @JoinColumn(name = "chef_id", nullable = false)
  private Long chefId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
