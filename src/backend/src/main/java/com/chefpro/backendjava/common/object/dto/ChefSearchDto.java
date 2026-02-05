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
public class ChefSearchDto {
  private Long userId;

  private String username;
  private String name;
  private String lastname;

  private String photo;
  private String bio;
  private String prizes;


  private BigDecimal startingPrice;

  private Double avgScore;

  private Long reviewsCount;
}
