package com.chefpro.backendjava.controller.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDto {

  private String id;

  private String name;

  private String email;

  private String photoURL;

  private String role;

}
