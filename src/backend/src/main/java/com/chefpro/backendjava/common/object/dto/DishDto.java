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
public class DishDto {

  private Long menuId;
  private Long dishId;
  private String title;
  private String description;
  private String category;
  private String creator;
  private List<String> allergens;
  private String photo;
}
