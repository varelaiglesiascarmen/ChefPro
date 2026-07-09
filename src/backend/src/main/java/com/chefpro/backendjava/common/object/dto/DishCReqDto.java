package com.chefpro.backendjava.common.object.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DishCReqDto {

  @JsonProperty("menu_ID")
  @JsonAlias({ "menu_id", "menuId" })
  private Long menuId;

  @JsonProperty("dish_ID")
  @JsonAlias({ "dish_id", "dishId" })
  private Long dishId;

  private String title;
  private String description;
  private String category;
  private List<String> allergens;
  private String photo;
}
