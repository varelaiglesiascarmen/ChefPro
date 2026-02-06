package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChefPublicDetailDto {

  private Long id;
  private String name;
  private String lastname;
  private String fullName;
  private String email;
  private String phoneNumber;
  private String photo;
  private String bio;
  private String prizes;
  private String location;
  private String languages;
  private String coverPhoto;

  private Double rating;
  private Long reviewsCount;

  private List<MenuSummaryDto> menus;
  private List<ReviewSummaryDto> reviews;
  private List<String> busyDates;
}
