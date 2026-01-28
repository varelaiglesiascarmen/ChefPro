package com.chefpro.backendjava.common.object.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
    private BigDecimal pricePerPerson;

    @Column(name = "is_delivery_available", nullable = false)
    private boolean deliveryAvailable;

    @Column(name = "can_cook_at_client_home", nullable = false)
    private boolean cookAtClientHome;

    @Column(name = "is_pickup_available", nullable = false)
    private boolean pickupAvailable;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chef_id", nullable = false)
    private UserLogin chef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
