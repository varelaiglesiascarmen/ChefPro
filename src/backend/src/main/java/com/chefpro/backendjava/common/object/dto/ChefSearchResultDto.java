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

  private List<String> cities;
  private List<ChefSearchDto> chefs;
  private List<MenuSearchDto> menus;

  // true when there were no real results and menus are random suggestions
  private boolean noResults;
}
