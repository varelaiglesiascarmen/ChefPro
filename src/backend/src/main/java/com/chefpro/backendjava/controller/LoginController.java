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
    Authentication auth = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );
    return ResponseEntity.ok(buildLoginResponse((UserDetails) auth.getPrincipal()));
  }

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

  @GetMapping("/me")
  public ResponseEntity<UserLoginDto> me(@AuthenticationPrincipal UserDetails user) {
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String role = extractRole(user);
    UserLoginDto dto = userService.findByEmail(user.getUsername());
    dto.setRole(role);
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/health")
  public String health() {
    return "OK";
  }

  @GetMapping("/check-username")
  public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
    return ResponseEntity.ok(!userService.existsByUsername(username));
  }

  @GetMapping("/check-email")
  public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
    return ResponseEntity.ok(!userService.existsByEmail(email));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.ok().build();
  }

  @PutMapping("/profile")
  public ResponseEntity<UserLoginDto> updateProfile(
      @AuthenticationPrincipal UserDetails user,
      @Valid @RequestBody UpdateProfileDto dto) {
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    try {
      return ResponseEntity.ok(userService.updateProfile(user.getUsername(), dto));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @DeleteMapping("/account")
  public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails user) {
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    try {
      userService.deleteAccount(user.getUsername());
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
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
