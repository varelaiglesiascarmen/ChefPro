package com.chefpro.backendjava.common.object.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plato {

    @Id
    @Column(length = 50)
    private String id;

    @Column(length = 150, nullable = false)
    private String title;

    @Column(length = 300)
    private String description;

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
