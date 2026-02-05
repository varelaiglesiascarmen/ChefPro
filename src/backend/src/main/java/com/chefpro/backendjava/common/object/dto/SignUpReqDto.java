package com.chefpro.backendjava.common.object.dto;

import lombok.Data;

@Data
public class SignUpReqDto {

  private String name;
  private String surname;
  private String username;
  private String email;
  private String password;
  private String role;
}


