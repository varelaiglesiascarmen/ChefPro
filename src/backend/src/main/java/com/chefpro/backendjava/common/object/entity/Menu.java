package com.chefpro.backendjava.common.object.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "menu_ID")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "chef_ID", nullable = false)
  private Chef chef;

  @Column(name = "title", length = 150, nullable = false)
  private String title;

  @Lob
  @Column(name = "description")
  private String description;

  @Column(name = "price_per_person", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerPerson;

  @Column(name = "min_number_diners")
  private Integer minNumberDiners;

  @Column(name = "max_number_diners")
  private Integer maxNumberDiners;

  @Lob
  @Column(name = "kitchen_requirements")
  private String kitchenRequirements;

  // Relación con dishes
  @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Dish> dishes = new ArrayList<>();

  // Relación con reservations (sin cascade para evitar borrado accidental de reservas)
  @OneToMany(mappedBy = "menu")
  @Builder.Default
  private List<Reservation> reservations = new ArrayList<>();

  // Método helper para añadir platos
  public void addPlato(Dish dish) {
    dishes.add(dish);
    dish.setMenu(this);
    dish.setMenuId(this.id);
  }

  // Método helper para remover platos
  public void removePlato(Dish dish) {
    dishes.remove(dish);
    dish.setMenu(null);
  }
}
