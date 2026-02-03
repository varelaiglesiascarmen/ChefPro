package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationsUReqDto {

  private Long id;
  private String name;
  private String description;
  private OffsetDateTime date;
  private String location;
  private Long customerId;
  private MenuDTO menu;
  private int numberOfSeats;

}
