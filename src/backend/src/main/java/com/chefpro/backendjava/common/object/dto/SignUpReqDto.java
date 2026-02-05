package com.chefpro.backendjava.common.object.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SignUpReqDto {

  private String name;
  private String surname;
  private String username;
  private String email;

  @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number")
  private String phoneNumber;
  private String password;
  private String role;
}


