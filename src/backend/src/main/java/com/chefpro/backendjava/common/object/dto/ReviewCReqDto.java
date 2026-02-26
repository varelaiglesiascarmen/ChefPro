package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCReqDto {

  private Long chefId;
  private LocalDate reservationDate;
  private Integer score;
  private String comment;
}
