package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChefSearchDto {

  private Long id;
  private String name;
  private String lastname;
  private String username;
  private String email;
  private String phoneNumber;
  private String photo;
  private String bio;
  private String prizes;
}
