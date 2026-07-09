package com.chefpro.backendjava.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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
      .headers(headers -> headers
        .httpStrictTransportSecurity(hsts -> hsts
          .includeSubDomains(true)
          .maxAgeInSeconds(31536000)
        )
      )
      .userDetailsService(usuarioDetailsService)
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authorizeHttpRequests(auth -> auth
        // Swagger
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
          "/swagger-resources/**", "/webjars/**").permitAll()

        // Auth
        .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/health",
          "/api/auth/logout", "/api/auth/check-username", "/api/auth/check-email").permitAll()

        // Static frontend
        .requestMatchers("/", "/index.html", "/favicon.ico", "/assets/**",
          "/static/**", "/public/**", "/**/*.css", "/**/*.js", "/**/*.map").permitAll()

        // Public chef/menu endpoints
        .requestMatchers(HttpMethod.GET, "/api/chef/menus/public").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/chef/search").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/chef/{chefId}/profile").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/chef/menus/{menuId}/public").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/public-profile/**").permitAll()

        // Chef-only
        .requestMatchers("/api/chef/**").hasAuthority("ROLE_CHEF")

        // Reservations
        .requestMatchers(HttpMethod.POST, "/api/reservations").hasAuthority("ROLE_DINER")
        .requestMatchers(HttpMethod.POST, "/api/reservations/review").hasAuthority("ROLE_DINER")
        .requestMatchers(HttpMethod.PATCH, "/api/reservations/status").authenticated()
        .requestMatchers(HttpMethod.DELETE, "/api/reservations").authenticated()
        .requestMatchers(HttpMethod.GET, "/api/reservations/chef").hasAuthority("ROLE_CHEF")
        .requestMatchers(HttpMethod.GET, "/api/reservations/comensal").hasAuthority("ROLE_DINER")

        // Role-specific
        .requestMatchers("/api/comensal/**").hasAuthority("ROLE_DINER")
        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

        // Authenticated user
        .requestMatchers("/api/auth/me").authenticated()
        .requestMatchers("/api/auth/profile").authenticated()

        .anyRequest().authenticated()
      )
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
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
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }
}
