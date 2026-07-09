package com.chefpro.backendjava.common.object.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

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
  @JsonAlias({ "pricePerPerson" })
  private BigDecimal pricePerPerson;

  @JsonProperty("min_number_diners")
  @JsonAlias({ "minNumberDiners" })
  private Integer minNumberDiners;

  @JsonProperty("max_number_diners")
  @JsonAlias({ "maxNumberDiners" })
  private Integer maxNumberDiners;

  @JsonProperty("kitchen_requirements")
  @JsonAlias({ "kitchenRequirements" })
  private String kitchenRequirements;

  @JsonProperty("chef_ID")
  @JsonAlias({ "chef_id", "chefId" })
  private Long chefId;

  @JsonProperty("dish_ids")
  @JsonAlias({ "dishIds" })
  private List<Long> dishIds;
}
