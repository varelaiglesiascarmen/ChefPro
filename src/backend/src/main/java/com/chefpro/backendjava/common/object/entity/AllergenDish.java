package com.chefpro.backendjava.common.object.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "allergens_dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(AllergenDish.AllergenDishId.class)
public class AllergenDish {

  @Id
  @Column(name = "menu_ID", nullable = false)
  private Long menuId;

  @Id
  @Column(name = "dish_ID", nullable = false)
  private Long dishId;

  @Id
  @Column(name = "allergen", length = 50, nullable = false)
  private String allergen;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumns({
    @JoinColumn(name = "menu_ID", referencedColumnName = "menu_ID", insertable = false, updatable = false),
    @JoinColumn(name = "dish_ID", referencedColumnName = "dish_ID", insertable = false, updatable = false)
  })
  private Dish dish;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "allergen", insertable = false, updatable = false)
  private OfficialAllergen officialAllergen;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AllergenDishId implements Serializable {
    private Long menuId;
    private Long dishId;
    private String allergen;
  }
}
