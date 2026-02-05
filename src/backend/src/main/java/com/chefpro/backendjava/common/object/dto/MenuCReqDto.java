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
public class MenuCReqDto {

  private String title;
  private String description;
  private BigDecimal pricePerPerson;

  private Integer minNumberDiners;
  private Integer maxNumberDiners;
  private String kitchenRequirements;

  private List<Long> dishIds;
}

