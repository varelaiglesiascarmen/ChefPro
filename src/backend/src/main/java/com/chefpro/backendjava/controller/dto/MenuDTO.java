package com.chefpro.backendjava.controller.dto;

import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDTO {

    private String id;
    private String title;
    private String description;

    private String dishes;
    private String allergens;

    private Double pricePerPerson;

    private boolean deliveryAvailable;
    private boolean cookAtClientHome;
    private boolean pickupAvailable;

    private String chefUsername;

    private Instant createdAt;
}
