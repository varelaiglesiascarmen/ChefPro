package com.chefpro.backendjava.common.object.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(Dish.DishId.class)
public class Dish {

  @Id
  @Column(name = "menu_ID", nullable = false)
  private Long menuId;

  @Id
  @Column(name = "dish_ID", nullable = false)
  private Long dishId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "menu_ID", insertable = false, updatable = false)
  private Menu menu;

  @Column(name = "title", length = 150, nullable = false)
  private String title;

  @Lob
  @Column(name = "description")
  private String description;

  @Column(name = "category", length = 50)
  private String category;

  @Lob
  @Column(name = "photo")
  private String photo;

  @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<AllergenDish> allergenDishes = new ArrayList<>();

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DishId implements Serializable {
    private Long menuId;
    private Long dishId;
  }
}
