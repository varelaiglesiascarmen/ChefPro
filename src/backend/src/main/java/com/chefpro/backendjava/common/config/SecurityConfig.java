package com.chefpro.backendjava.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
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
      .cors(Customizer.withDefaults())
      .csrf(csrf -> csrf.disable())
      .userDetailsService(usuarioDetailsService)
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authorizeHttpRequests(auth -> auth
        // ========================================
        // SWAGGER/OPENAPI - DEBE ESTAR PRIMERO
        // ========================================
        .requestMatchers(
          "/swagger-ui/**",
          "/v3/api-docs/**",
          "/swagger-ui.html",
          "/swagger-resources/**",
          "/webjars/**"
        ).permitAll()

        // ========================================
        // RUTAS DE AUTENTICACIÓN PÚBLICAS
        // ========================================
        .requestMatchers(
          "/api/auth/login",
          "/api/auth/signup",
          "/api/auth/health",
          "/api/auth/logout",
          "/api/auth/check-username",
          "/api/auth/check-email"
        ).permitAll()

        // ========================================
        // RECURSOS ESTÁTICOS PÚBLICOS (Frontend)
        // ========================================
        .requestMatchers(
          "/", "/index.html", "/favicon.ico",
          "/assets/**", "/static/**", "/public/**",
          "/**/*.css", "/**/*.js", "/**/*.map"
        ).permitAll()

        // ========================================
        // ENDPOINTS PÚBLICOS DE MENÚS
        // ========================================
        .requestMatchers(HttpMethod.GET, "/api/chef/menus/public").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/chef/search").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/chef/*/profile").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/chef/menus/*/public").permitAll()

        // ========================================
        // RUTAS PROTEGIDAS POR ROL - CHEF
        // ========================================
        .requestMatchers("/api/chef/**").hasAuthority("ROLE_CHEF")

        // ========================================
        // RUTAS DE RESERVACIONES
        // ========================================
        // Crear reserva - solo DINER
        .requestMatchers(HttpMethod.POST, "/api/reservations").hasAuthority("ROLE_DINER")

        // Actualizar estado de reserva - solo CHEF
        .requestMatchers(HttpMethod.PATCH, "/api/reservations/status").hasAuthority("ROLE_CHEF")

        // Eliminar reserva - cualquier usuario autenticado (chef o diner propietario)
        .requestMatchers(HttpMethod.DELETE, "/api/reservations").authenticated()

        // Ver reservas del chef - solo CHEF
        .requestMatchers(HttpMethod.GET, "/api/reservations/chef").hasAuthority("ROLE_CHEF")

        // Ver reservas del comensal - solo DINER
        .requestMatchers(HttpMethod.GET, "/api/reservations/comensal").hasAuthority("ROLE_DINER")

        // ========================================
        // RUTAS PROTEGIDAS POR ROL - COMENSAL
        // ========================================
        .requestMatchers("/api/comensal/**").hasAuthority("ROLE_DINER")

        // ========================================
        // RUTAS PROTEGIDAS POR ROL - ADMIN
        // ========================================
        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

        // ========================================
        // ENDPOINT DE USUARIO AUTENTICADO
        // ========================================
        .requestMatchers("/api/auth/me").authenticated()
        .requestMatchers("/api/auth/profile").authenticated()

        // ========================================
        // CUALQUIER OTRA RUTA REQUIERE AUTENTICACIÓN
        // ========================================
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
