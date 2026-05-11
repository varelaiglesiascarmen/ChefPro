package com.chefpro.backendjava.common.object.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuCReqDto {

  @NotBlank(message = "title is required")
  private String title;

  private String description;

  @NotNull(message = "pricePerPerson is required")
  @DecimalMin(value = "0.01", message = "pricePerPerson must be greater than 0")
  @JsonProperty("price_per_person")
  private BigDecimal pricePerPerson;

  private Integer minNumberDiners;
  private Integer maxNumberDiners;
  private String kitchenRequirements;

  private Long chefId;

  private List<Long> dishIds;
}
