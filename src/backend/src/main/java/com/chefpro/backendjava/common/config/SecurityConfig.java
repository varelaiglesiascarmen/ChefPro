package com.chefpro.backendjava.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.chefpro.backendjava.common.security.CustomUserDetailsService;
import com.chefpro.backendjava.common.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final CustomUserDetailsService usuarioDetailsService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(CustomUserDetailsService usuarioDetailsService,
                        JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.usuarioDetailsService = usuarioDetailsService;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .userDetailsService(usuarioDetailsService)
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/login", "/api/auth/health").permitAll()
        .requestMatchers(
          "/", "/index.html", "/favicon.ico",
          "/assets/**", "/static/**", "/public/**",
          "/**/*.css", "/**/*.js", "/**/*.map"
        ).permitAll()

        .requestMatchers("/api/chef/**").hasAuthority("ROLE_CHEF")  // Cambiado a hasAuthority
        .requestMatchers("/api/reservations/chef/**").hasAuthority("ROLE_CHEF")
        .requestMatchers("/api/reservations/comensal/**").hasAuthority("ROLE_DINER")
        .requestMatchers("/api/reservations/**").authenticated()
        .requestMatchers("/api/comensal/**").hasAuthority("ROLE_DINER")
        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .formLogin(form -> form.disable())
      .httpBasic(basic -> basic.disable());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration authConfig
  ) throws Exception {
    return authConfig.getAuthenticationManager();
  }
}
