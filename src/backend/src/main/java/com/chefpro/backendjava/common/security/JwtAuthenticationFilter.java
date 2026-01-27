package com.chefpro.backendjava.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final CustomUserDetailsService userDetailsService;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {

    // Extraer el header Authorization
    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // Extraer el token (quitar "Bearer ")
      String token = authHeader.substring(7);

      // Extraer username del token
      String username = jwtUtil.extractUsername(token);

      // Si el token es válido y no hay autenticación ya establecida
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        // Cargar detalles del usuario
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Validar el token
        if (jwtUtil.validateToken(token, userDetails)) {

          // Crear objeto de autenticación
          UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
            );

          authToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
          );

          // Establecer autenticación en el contexto de seguridad
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (Exception e) {
      // Log del error si es necesario
      logger.error("Error procesando JWT: " + e.getMessage());
    }

    filterChain.doFilter(request, response);
  }
}
