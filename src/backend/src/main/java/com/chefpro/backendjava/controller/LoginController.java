package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.security.JwtUtil;
import com.chefpro.backendjava.common.object.dto.SignUpReqDto;
import com.chefpro.backendjava.common.object.dto.login.LoginRequestDto;
import com.chefpro.backendjava.common.object.dto.login.LoginResponseDto;
import com.chefpro.backendjava.common.object.dto.login.UpdateProfileDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;
import com.chefpro.backendjava.service.UserService;
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

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {

    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
        request.getUsername(),
        request.getPassword()
      )
    );

    UserDetails user = (UserDetails) authentication.getPrincipal();
    String token = jwtUtil.generateToken(user);

    String role = user.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .findFirst()
      .orElse(null);

    UserLoginDto userLoginDto = userService.findByEmail(request.getUsername());
    // Actualizar el rol con el formato correcto
    userLoginDto.setRole(role);

    LoginResponseDto response = new LoginResponseDto();
    response.setToken(token);
    response.setUser(userLoginDto);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/health")
  public String health() {
    return "OK";
  }

  @GetMapping("/me")
  public ResponseEntity<UserLoginDto> me(@AuthenticationPrincipal UserDetails user) {
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String role = user.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .findFirst()
      .orElse(null);

    UserLoginDto userLoginDto = userService.findByEmail(user.getUsername());
    // Actualizar el rol con el formato correcto
    userLoginDto.setRole(role);

    return ResponseEntity.ok(userLoginDto);
  }

  @PostMapping("/signup")
  public ResponseEntity<LoginResponseDto> signup(@RequestBody @Valid SignUpReqDto request) {

    if (userService.signUp(request)) {

      // Auto-login después del registro (usa username/password del signup)
      Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
          request.getUsername(),
          request.getPassword()
        )
      );

      UserDetails user = (UserDetails) authentication.getPrincipal();
      String token = jwtUtil.generateToken(user);

      String role = user.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .findFirst()
        .orElse(null);

      UserLoginDto userLoginDto = userService.findByEmail(request.getUsername());
      // Actualizar el rol con el formato correcto
      userLoginDto.setRole(role);

      LoginResponseDto response = new LoginResponseDto();
      response.setToken(token);
      response.setUser(userLoginDto);

      return ResponseEntity.status(201).body(response);
    }

    return ResponseEntity.status(400).build();
  }

  @GetMapping("/check-username")
  public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
    boolean exists = userService.existsByUsername(username);
    // Return true if available (not taken), false if taken
    return ResponseEntity.ok(!exists);
  }

  @GetMapping("/check-email")
  public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
    boolean exists = userService.existsByEmail(email);
    return ResponseEntity.ok(!exists);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.ok().build();
  }

  @PutMapping("/profile")
  public ResponseEntity<UserLoginDto> updateProfile(
      @AuthenticationPrincipal UserDetails user,
      @Valid @RequestBody UpdateProfileDto updateProfileDto) {
    
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      UserLoginDto updatedUser = userService.updateProfile(user.getUsername(), updateProfileDto);
      return ResponseEntity.ok(updatedUser);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }
}
