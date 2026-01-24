package com.chefpro.backendjava.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

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
    private String id;

    @Column(length = 150, nullable = false)
    private String title;

    @Column(length = 300)
    private String description;

    // Guardamos lista de platos como texto separado por comas
    @Column(nullable = false, columnDefinition = "TEXT")
    private String dishes;

    // Guardamos lista de alérgenos como texto separado por comas
    @Column(nullable = false, columnDefinition = "TEXT")
    private String allergens;

    @Column(name = "price_per_person", nullable = false, precision = 10, scale = 2)
    private Double pricePerPerson;

    @Column(name = "is_delivery_available", nullable = false)
    private boolean deliveryAvailable;

    @Column(name = "can_cook_at_client_home", nullable = false)
    private boolean cookAtClientHome;

    @Column(name = "is_pickup_available", nullable = false)
    private boolean pickupAvailable;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chef_id", nullable = false)
    private CustomUser chef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
