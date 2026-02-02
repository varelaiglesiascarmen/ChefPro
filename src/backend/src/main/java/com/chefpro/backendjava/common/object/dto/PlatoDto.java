package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlatoDto {

  private Long id;
  private String name;
  private String description;
  private String creator;
  private Boolean vegan;
  private Boolean vegetarian;
  private Boolean allergies;
  private List<IngredientsDto> ingredients;
}
