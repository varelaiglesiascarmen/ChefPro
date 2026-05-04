package com.chefpro.backendjava.common.object.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpReqDto {

  @NotBlank(message = "name is required")
  private String name;

  @NotBlank(message = "surname is required")
  private String surname;

  @NotBlank(message = "username is required")
  @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
  private String username;

  @NotBlank(message = "email is required")
  @Email(message = "email must be a valid email address")
  private String email;

  @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number")
  private String phoneNumber;

  @NotBlank(message = "password is required")
  @Size(min = 8, message = "password must be at least 8 characters")
  private String password;

  private String role;
}


