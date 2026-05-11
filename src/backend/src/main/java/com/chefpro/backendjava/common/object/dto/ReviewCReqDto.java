package com.chefpro.backendjava.common.object.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCReqDto {

  @NotNull(message = "chefId is required")
  private Long chefId;

  @NotNull(message = "reservationDate is required")
  @Past(message = "reservationDate must be in the past")
  private LocalDate reservationDate;

  @NotNull(message = "score is required")
  @Min(value = 1, message = "score must be at least 1")
  @Max(value = 5, message = "score must be at most 5")
  private Integer score;

  private String comment;
}
