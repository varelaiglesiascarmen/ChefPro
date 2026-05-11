package com.chefpro.backendjava.common.object.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

  @NotNull(message = "chefId is required")
  private Long chefId;

  @NotNull(message = "date is required")
  @Future(message = "date must be in the future")
  private LocalDate date;

  @NotNull(message = "menuId is required")
  private Long menuId;

  @NotNull(message = "numberOfDiners is required")
  @Min(value = 1, message = "numberOfDiners must be at least 1")
  private Integer numberOfDiners;

  @NotBlank(message = "address is required")
  private String address;
}
