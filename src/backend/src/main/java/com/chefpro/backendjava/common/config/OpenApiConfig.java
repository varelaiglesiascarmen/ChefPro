package com.chefpro.backendjava.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI chefProOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("ChefPro API")
            .description("API REST para la plataforma ChefPro: conecta chefs profesionales con comensales.")
            .version("1.0.0"))
        .components(new Components()
            .addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Introduce el token JWT obtenido en /api/auth/login")));
  }
}
