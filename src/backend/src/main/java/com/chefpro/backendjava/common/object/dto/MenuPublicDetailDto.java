package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuPublicDetailDto {

  private Long id;
  private String title;
  private String description;
  private BigDecimal price;
  private Integer minDiners;
  private Integer maxDiners;
  private String requirements;

  private Long chefId;
  private String chefName;
  private String chefPhoto;

  private List<DishPublicDto> dishes;
  private List<String> busyDates;
}
