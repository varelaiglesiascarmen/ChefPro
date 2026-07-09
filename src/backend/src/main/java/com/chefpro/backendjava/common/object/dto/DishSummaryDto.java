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
public class DishSummaryDto {

  private Long menuId;
  private Long dishId;
  private String title;
  private String description;
  private String category;
  private List<String> allergens;
  private String photo;
}
