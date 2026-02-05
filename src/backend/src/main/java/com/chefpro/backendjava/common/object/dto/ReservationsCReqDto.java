package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationsCReqDto {

  private Long chefId;
  private LocalDate date;
  private Long menuId;
  private Integer numberOfDiners;
  private String address;
}
