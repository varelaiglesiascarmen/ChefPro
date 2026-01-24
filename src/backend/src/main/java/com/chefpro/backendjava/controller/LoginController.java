package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.controller.dto.login.LoginRequestDto;
import com.chefpro.backendjava.common.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {


    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    public LoginController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequestDto request){

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

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", role);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }
}
