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
public class MenuSearchDto {

  private Long menuId;
  private String menuTitle;
  private String menuDescription;
  private BigDecimal pricePerPerson;
  private Integer minDiners;
  private Integer maxDiners;

  // Info del chef propietario del menú
  private Long chefId;
  private String chefName;
  private String chefLastname;
  private String chefPhoto;
  private String chefLocation;
  private Double avgScore;
  private Long reviewsCount;
}
