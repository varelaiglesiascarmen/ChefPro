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
        // Endpoints públicos
        .requestMatchers("/api/auth/login", "/api/auth/health").permitAll()
        // Permitir raíz y recursos estáticos
        .requestMatchers(
          "/", "/index.html", "/favicon.ico",
          "/assets/**", "/static/**", "/public/**",
          "/**/*.css", "/**/*.js", "/**/*.map"
        ).permitAll()

        // Endpoints protegidos por rol
        .requestMatchers("/api/chef/**").hasRole("CHEF")
        .requestMatchers("/api/comensal/**").hasRole("COMENSAL")
        .requestMatchers("/api/admin/**").hasRole("ADMIN")

        // Todo lo demás requiere autenticación (incluyendo /api/auth/me)
        .anyRequest().authenticated()
      )
      // Agregar filtro JWT ANTES del filtro de autenticación por defecto
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      // Deshabilitar form login y http basic
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
