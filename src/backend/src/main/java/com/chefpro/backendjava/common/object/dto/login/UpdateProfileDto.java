package com.chefpro.backendjava.common.object.dto.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileDto {
  
  @NotBlank(message = "El nombre es obligatorio")
  @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
  private String name;
  
  @NotBlank(message = "El apellido es obligatorio")
  @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
  private String surname;
  
  @NotBlank(message = "El nombre de usuario es obligatorio")
  @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
  private String username;
  
  private String photo;  // Base64 image
  
  // Campos específicos para Chef
  private String bio;
  private String prizes;
  private String address;
}
