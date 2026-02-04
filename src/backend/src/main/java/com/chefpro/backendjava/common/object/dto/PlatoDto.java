package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlatoDto {

  private Long id;
  private String title;
  private String description;
  private String creator;
  private Boolean vegan;
  private Boolean vegetarian;
  private Boolean allergies;
  private List<IngredientsDto> ingredients;
}
