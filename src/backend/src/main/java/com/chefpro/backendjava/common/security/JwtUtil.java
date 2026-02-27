package com.chefpro.backendjava.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

  private final SecretKey SECRET_KEY;
  private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 horas

  public JwtUtil(@Value("${jwt.secret}") String secret) {
    this.SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    String role = userDetails.getAuthorities().stream()
      .map(auth -> auth.getAuthority())
      .findFirst()
      .orElse(null);
    claims.put("role", role);
    return createToken(claims, userDetails.getUsername());
  }

  private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
      .setClaims(claims)
      .setSubject(subject)
      .setIssuedAt(new Date(System.currentTimeMillis()))
      .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
      .signWith(SECRET_KEY)
      .compact();
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(SECRET_KEY)
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
}
