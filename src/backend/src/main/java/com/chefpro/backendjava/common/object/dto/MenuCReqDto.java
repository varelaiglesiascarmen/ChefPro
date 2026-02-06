package com.chefpro.backendjava.common.object.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCReqDto {

  private String title;
  private String description;

  @JsonProperty("price_per_person")
  private BigDecimal pricePerPerson;

  @JsonProperty("min_number_diners")
  private Integer minNumberDiners;

  @JsonProperty("max_number_diners")
  private Integer maxNumberDiners;

  @JsonProperty("kitchen_requirements")
  private String kitchenRequirements;

  private List<Long> dishIds;
}
