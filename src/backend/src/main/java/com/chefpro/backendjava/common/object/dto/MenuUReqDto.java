package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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

    private BigDecimal pricePerPerson;

    private boolean deliveryAvailable;
    private boolean cookAtClientHome;
    private boolean pickupAvailable;

    private String chefUsername;

    private Instant createdAt;
}
