package com.chefpro.backendjava.common.object.dto.login;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDto {

  private Long id;

  private String name;

  private String username;

  private String surname;

  private String email;

  private String phoneNumber;

  private String role;

  private String photoUrl;

}
