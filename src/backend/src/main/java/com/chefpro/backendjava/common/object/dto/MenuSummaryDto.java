package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuSummaryDto {

  private Long id;
  private String title;
  private String description;
  private BigDecimal price;
  private Integer dishesCount;
  private Integer minDiners;
  private Integer maxDiners;
}
