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
public class DishPublicDto {

  private Long dishId;
  private String title;
  private String description;
  private String category;
  private List<Integer> allergenIds;
}
