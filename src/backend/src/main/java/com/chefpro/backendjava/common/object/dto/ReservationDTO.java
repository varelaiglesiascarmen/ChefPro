package com.chefpro.backendjava.common.object.dto;

import com.chefpro.backendjava.common.object.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationDTO {

  private Long chefId;
  private LocalDate date;
  private Long dinerId;
  private Long menuId;
  private Integer numberOfDiners;
  private String address;
  private Reservation.ReservationStatus status;


  private BigDecimal totalPrice;

  private String chefName;
  private String dinerName;
  private String menuTitle;
}
