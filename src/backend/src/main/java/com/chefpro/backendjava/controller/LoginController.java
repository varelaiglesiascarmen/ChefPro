package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.security.JwtUtil;
import com.chefpro.backendjava.common.object.dto.login.LoginRequestDto;
import com.chefpro.backendjava.common.object.dto.login.LoginResponseDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;
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


    public LoginController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }


  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request){

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

    // mapear a UserDto (ajusta según tu entidad real)
    UserLoginDto userLoginDto = new UserLoginDto();
    userLoginDto.setId(user.getUsername());   // o el id real si lo tienes
    userLoginDto.setName(user.getUsername()); // o nombre completo
    userLoginDto.setEmail(null);              // si no tienes email todavía
    userLoginDto.setPhotoURL(null);
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

    UserLoginDto userLoginDto = new UserLoginDto();
    userLoginDto.setId(user.getUsername());
    userLoginDto.setName(user.getUsername());
    userLoginDto.setEmail(null);
    userLoginDto.setPhotoURL(null);
    userLoginDto.setRole(role);

    return ResponseEntity.ok(userLoginDto);
  }
}
