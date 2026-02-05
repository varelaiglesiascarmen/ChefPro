package com.chefpro.backendjava.common.object.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "official_allergens_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficialAllergen {

  @Id
  @Column(name = "allergen_name", length = 50, nullable = false)
  private String allergenName;

  // Relación con allergens_dishes
  @OneToMany(mappedBy = "officialAllergen")
  @Builder.Default
  private List<AllergenDish> allergenDishes = new ArrayList<>();
}
