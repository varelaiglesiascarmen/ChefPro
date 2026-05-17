package com.chefpro.backendjava.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chefpro.backendjava.common.object.dto.ChefPublicDetailDto;
import com.chefpro.backendjava.service.ChefProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Usuarios", description = "Endpoints disponibles para todos los usuarios")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ChefProfileService chefProfileService;

    public UserController(ChefProfileService chefProfileService) {
        this.chefProfileService = chefProfileService;
    }

    @Operation(summary = "Obtener perfil público de un chef", description = "Devuelve los detalles públicos de un chef dado su ID.")
    @GetMapping("/public-profile/{id}")
    public ResponseEntity<ChefPublicDetailDto> getPublicProfile(@PathVariable Long id) {
        ChefPublicDetailDto chefProfile = chefProfileService.getChefPublicProfile(id);
        return ResponseEntity.ok(chefProfile);
    }

    
}
