package com.chefpro.backendjava.common.object.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuUReqDto {


    private Long id;
    private String title;
    private String description;

    private List<Long> dishes;
    private String allergens;

    @JsonProperty("price_per_person")
    private BigDecimal pricePerPerson;

    @JsonProperty("min_number_diners")
    private Integer minNumberDiners;

    @JsonProperty("max_number_diners")
    private Integer maxNumberDiners;

    @JsonProperty("kitchen_requirements")
    private String kitchenRequirements;

    private boolean deliveryAvailable;
    private boolean cookAtClientHome;
    private boolean pickupAvailable;

    private String chefUsername;

    private Instant createdAt;
}
