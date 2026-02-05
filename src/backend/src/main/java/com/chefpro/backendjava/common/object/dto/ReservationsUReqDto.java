package com.chefpro.backendjava.common.object.dto;

import com.chefpro.backendjava.common.object.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationsUReqDto {

  private Long chefId;
  private java.time.LocalDate date;


  private Integer numberOfDiners;
  private String address;
  private Long menuId;
  private Reservation.ReservationStatus status;
}
