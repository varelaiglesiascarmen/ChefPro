package com.chefpro.backendjava.common.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

  @Value("${cors.allowed-origins}")
  private String allowedOriginsRaw;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    List<String> allowedOrigins = Arrays.asList(allowedOriginsRaw.split(","));

    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = Arrays.asList(allowedOriginsRaw.split(","));
        registry.addMapping("/**")
          .allowedOrigins(allowedOrigins.toArray(new String[0]))
          .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
          .allowedHeaders("*")
          .allowCredentials(true)
          .maxAge(3600);
      }
    };
  }
}
