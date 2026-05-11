package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.security.JwtUtil;
import com.chefpro.backendjava.common.object.dto.SignUpReqDto;
import com.chefpro.backendjava.common.object.dto.login.LoginRequestDto;
import com.chefpro.backendjava.common.object.dto.login.LoginResponseDto;
import com.chefpro.backendjava.common.object.dto.login.UpdateProfileDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;
import com.chefpro.backendjava.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Registro, login y gestión del perfil de usuario")
@RestController
@RequestMapping("api/auth")
public class LoginController {

  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final UserService userService;

  public LoginController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.userService = userService;
  }

  @Operation(summary = "Iniciar sesión", description = "Autentica al usuario con username y contraseña. Devuelve un token JWT y los datos del usuario.")
  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
    Authentication auth = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );
    return ResponseEntity.ok(buildLoginResponse((UserDetails) auth.getPrincipal()));
  }

  @Operation(summary = "Registrar usuario", description = "Crea una cuenta nueva (CHEF o DINER) y devuelve el token JWT para iniciar sesión directamente.")
  @PostMapping("/signup")
  public ResponseEntity<LoginResponseDto> signup(@RequestBody @Valid SignUpReqDto request) {
    if (!userService.signUp(request)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    Authentication auth = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(buildLoginResponse((UserDetails) auth.getPrincipal()));
  }

  @Operation(summary = "Obtener usuario autenticado", description = "Devuelve los datos del usuario que corresponde al token JWT enviado en la cabecera.")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/me")
  public ResponseEntity<UserLoginDto> me(@AuthenticationPrincipal UserDetails user) {
    String role = extractRole(user);
    UserLoginDto dto = userService.findByEmail(user.getUsername());
    dto.setRole(role);
    return ResponseEntity.ok(dto);
  }

  @Operation(summary = "Health check", description = "Comprueba que el servidor está levantado. Devuelve 'OK'.")
  @GetMapping("/health")
  public String health() {
    return "OK";
  }

  @Operation(summary = "Comprobar disponibilidad de username", description = "Devuelve true si el username está disponible, false si ya está en uso.")
  @GetMapping("/check-username")
  public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
    return ResponseEntity.ok(!userService.existsByUsername(username));
  }

  @Operation(summary = "Comprobar disponibilidad de email", description = "Devuelve true si el email está disponible, false si ya está registrado.")
  @GetMapping("/check-email")
  public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
    return ResponseEntity.ok(!userService.existsByEmail(email));
  }

  @Operation(summary = "Cerrar sesión", description = "Endpoint simbólico de logout. El token JWT se invalida en el cliente eliminándolo.")
  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Actualizar perfil de usuario", description = "Permite al usuario autenticado actualizar sus datos personales (nombre, foto, dirección, etc.).")
  @SecurityRequirement(name = "bearerAuth")
  @PutMapping("/profile")
  public ResponseEntity<UserLoginDto> updateProfile(
      @AuthenticationPrincipal UserDetails user,
      @Valid @RequestBody UpdateProfileDto dto) {
    return ResponseEntity.ok(userService.updateProfile(user.getUsername(), dto));
  }

  @Operation(summary = "Eliminar cuenta", description = "Borra permanentemente la cuenta del usuario autenticado y todos sus datos asociados.")
  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping("/account")
  public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails user) {
    userService.deleteAccount(user.getUsername());
    return ResponseEntity.ok().build();
  }

  private LoginResponseDto buildLoginResponse(UserDetails user) {
    String token = jwtUtil.generateToken(user);
    UserLoginDto dto = userService.findByEmail(user.getUsername());
    dto.setRole(extractRole(user));
    LoginResponseDto response = new LoginResponseDto();
    response.setToken(token);
    response.setUser(dto);
    return response;
  }

  private String extractRole(UserDetails user) {
    return user.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .findFirst()
      .orElse(null);
  }
}
