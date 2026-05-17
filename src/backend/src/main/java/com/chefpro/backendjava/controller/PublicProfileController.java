package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.object.dto.ChefPublicDetailDto;
import com.chefpro.backendjava.common.object.dto.PublicProfileDto;
import com.chefpro.backendjava.service.ChefProfileService;
import com.chefpro.backendjava.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Perfil Público", description = "Perfil público de chefs y comensales")
@RestController
@RequestMapping("/api/public-profile")
public class PublicProfileController {

    private final ChefProfileService chefProfileService;
    private final UserService userService;

    public PublicProfileController(ChefProfileService chefProfileService, UserService userService) {
        this.chefProfileService = chefProfileService;
        this.userService = userService;
    }

    @Operation(summary = "Obtener perfil público de un chef", description = "Devuelve los detalles públicos de un chef dado su ID de chef entity.")
    @GetMapping("/chef/{id}")
    public ResponseEntity<ChefPublicDetailDto> getChefPublicProfile(@PathVariable Long id) {
        ChefPublicDetailDto chefProfile = chefProfileService.getChefPublicProfile(id);
        return ResponseEntity.ok(chefProfile);
    }

    @Operation(summary = "Obtener perfil público de un usuario", description = "Devuelve los detalles públicos de un usuario (comensal) dado su ID.")
    @GetMapping("/user/{id}")
    public ResponseEntity<PublicProfileDto> getUserPublicProfile(@PathVariable Long id) {
        PublicProfileDto userProfile = userService.getUserPublicProfile(id);
        return ResponseEntity.ok(userProfile);
    }

    @Operation(summary = "Obtener perfil público de un usuario", description = "Devuelve los detalles públicos de cualquier usuario dado su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<PublicProfileDto> getPublicProfile(@PathVariable Long id) {
        PublicProfileDto profile = userService.getPublicProfile(id);
        return ResponseEntity.ok(profile);
    }
}
