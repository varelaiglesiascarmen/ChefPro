package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for partial chef profile updates (PATCH /api/chef/profile).
 * All fields are optional: only non-null fields are updated.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChefUReqDto {

  private String photo;
  private String bio;
  private String prizes;
  private String location;
  private String languages;
  private String coverPhoto;
}
