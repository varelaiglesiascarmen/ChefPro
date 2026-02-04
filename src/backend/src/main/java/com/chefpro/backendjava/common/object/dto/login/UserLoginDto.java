package com.chefpro.backendjava.common.object.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDto {

  private Long id;

  private String name;

  private String email;

  private String phoneNumber;

  private String role;

}
