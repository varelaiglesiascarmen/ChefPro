package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientsDto {

  private Long id;
  private String name;
  private List<String> category;
}
