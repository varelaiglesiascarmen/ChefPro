package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChefSearchResultDto {

  private List<ChefSearchDto> chefs;
  private List<MenuSearchDto> menus;

  // true cuando no hubo resultados reales y los menús son sugerencias aleatorias
  private boolean noResults;
}
