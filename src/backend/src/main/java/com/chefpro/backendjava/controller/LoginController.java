package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.security.JwtUtil;
import com.chefpro.backendjava.common.object.dto.SignUpReqDto;          // ✅ AÑADIR
import com.chefpro.backendjava.common.object.dto.login.LoginRequestDto;
import com.chefpro.backendjava.common.object.dto.login.LoginResponseDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;
import com.chefpro.backendjava.service.UserService;
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

    // OJO: aquí estás usando findByEmail pero le pasas username.
    // Lo dejo igual porque pides cambios mínimos.
    UserLoginDto userFound = userService.findByEmail(request.getUsername());

    UserLoginDto userLoginDto = new UserLoginDto();
    userLoginDto.setId(userFound.getId());
    userLoginDto.setName(userFound.getName());
    userLoginDto.setEmail(userFound.getEmail());
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

    UserLoginDto userFound = userService.findByEmail(user.getUsername());

    UserLoginDto userLoginDto = new UserLoginDto();
    userLoginDto.setId(userFound.getId());
    userLoginDto.setName(userFound.getName());
    userLoginDto.setEmail(userFound.getEmail());
    userLoginDto.setRole(role);

    return ResponseEntity.ok(userLoginDto);
  }

  @PostMapping("/signup")
  public ResponseEntity<LoginResponseDto> signup(@RequestBody SignUpReqDto request) {

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

      UserLoginDto userFound = userService.findByEmail(request.getUsername());

      UserLoginDto userLoginDto = new UserLoginDto();
      userLoginDto.setId(userFound.getId());
      userLoginDto.setName(userFound.getName());
      userLoginDto.setEmail(userFound.getEmail());
      userLoginDto.setRole(role);

      LoginResponseDto response = new LoginResponseDto();
      response.setToken(token);
      response.setUser(userLoginDto);

      return ResponseEntity.status(201).body(response);
    }

    return ResponseEntity.status(400).build();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.ok().build();
  }
}
