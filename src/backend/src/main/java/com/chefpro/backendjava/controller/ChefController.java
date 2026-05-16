package com.chefpro.backendjava.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.service.*;

@Tag(name = "Chef", description = "Gestión de menús, platos, perfil y búsqueda de chefs")
@RestController
@RequestMapping("/api/chef")
public class ChefController {

  private final MenuService menuService;
  private final DishService dishService;
  private final ChefSearchService chefSearchService;
  private final ChefProfileService chefProfileService;
  private final PhotoUploadService photoUploadService;

  public ChefController(MenuService menuService,
                        DishService dishService,
                        ChefSearchService chefSearchService,
                        ChefProfileService chefProfileService,
                        PhotoUploadService photoUploadService) {
    this.menuService = menuService;
    this.dishService = dishService;
    this.chefSearchService = chefSearchService;
    this.chefProfileService = chefProfileService;
    this.photoUploadService = photoUploadService;
  }

  // Menus

  @Operation(summary = "Listar menús del chef", description = "Devuelve todos los menús creados por el chef autenticado, incluyendo sus platos y alérgenos.")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/menus")
  public List<MenuDTO> listarMenusChef(Authentication authentication) {
    return menuService.listByChef(authentication);
  }

  @Operation(summary = "Crear menú", description = "Crea un nuevo menú asociado al chef autenticado. Requiere título y precio por persona.")
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/menus")
  public ResponseEntity<MenuDTO> createMenu(@RequestBody @Valid MenuCReqDto dto, Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createMenu(dto, authentication));
  }

  @Operation(summary = "Eliminar menú", description = "Elimina un menú del chef autenticado. No se puede eliminar si tiene reservas activas asociadas.")
  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping("/menus/{id}")
  public ResponseEntity<Void> eliminarMenu(
    Authentication authentication,
    @PathVariable Long id
  ) {
    menuService.deleteMenu(authentication, id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Actualizar menú", description = "Actualiza los campos del menú indicados en el cuerpo. Solo se modifican los campos que se envíen.")
  @SecurityRequirement(name = "bearerAuth")
  @PatchMapping("/menus")
  public MenuDTO actualizarMenu(
    Authentication authentication,
    @RequestBody MenuUReqDto menuUpdateDto
  ) {
    return menuService.updateMenu(authentication, menuUpdateDto);
  }

  @Operation(summary = "Listar todos los menús públicos", description = "Devuelve todos los menús de la plataforma. Endpoint público, no requiere autenticación.")
  @GetMapping("/menus/public")
  public ResponseEntity<List<MenuDTO>> listarMenusPublicos() {
    List<MenuDTO> menus = menuService.listAllMenus();
    return ResponseEntity.ok(menus);
  }

  // Dishes

  @Operation(summary = "Listar platos del chef", description = "Devuelve los platos del chef autenticado. Se puede filtrar por nombre con el parámetro 'nombrePlato'.")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/plato")
  public List<DishDto> getDishes(Authentication authentication,
                                 @RequestParam(required = false) String nombrePlato) {
    return dishService.getDish(authentication, nombrePlato);
  }

  @Operation(summary = "Crear plato", description = "Añade un nuevo plato a un menú del chef autenticado. Se pueden asociar alérgenos oficiales.")
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/plato")
  public ResponseEntity<Void> createDish(Authentication authentication, @RequestBody DishCReqDto dto) {
    dishService.createDish(authentication, dto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(summary = "Eliminar plato", description = "Elimina un plato de un menú del chef autenticado identificado por menuId y dishId.")
  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping("/plato")
  public ResponseEntity<Void> deleteDish(Authentication authentication,
                                         @RequestParam Long menuId,
                                         @RequestParam Long dishId) {
    dishService.deleteDish(authentication, menuId, dishId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Actualizar plato", description = "Modifica los campos del plato indicados. Si se envía la lista de alérgenos, reemplaza completamente la anterior.")
  @SecurityRequirement(name = "bearerAuth")
  @PatchMapping("/plato")
  public ResponseEntity<DishDto> updateDish(Authentication authentication, @RequestBody DishUReqDto dto) {
    return ResponseEntity.ok(dishService.updateDish(authentication, dto));
  }

  // Search

  @Operation(summary = "Buscar chefs y menús", description = "Búsqueda pública con filtros opcionales: texto libre, fecha de disponibilidad, rango de precio, número de comensales y alérgenos a excluir. Si no hay resultados, devuelve sugerencias aleatorias.")
  @GetMapping("/search")
  public ResponseEntity<ChefSearchResultDto> searchChefs(
    @RequestParam(required = false) String q,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
    @RequestParam(required = false) BigDecimal minPrice,
    @RequestParam(required = false) BigDecimal maxPrice,
    @RequestParam(required = false) Integer guests,
    @RequestParam(required = false) List<String> allergens) {
    return ResponseEntity.ok(chefSearchService.search(q, date, minPrice, maxPrice, guests, allergens));
  }

  // Public profile

  @Operation(summary = "Ver perfil público de un chef", description = "Devuelve el perfil completo de un chef: datos personales, menús, reseñas y fechas ocupadas. Endpoint público.")
  @GetMapping("/{chefId}/profile")
  public ResponseEntity<ChefPublicDetailDto> getChefPublicProfile(@PathVariable Long chefId) {
    try {
      return ResponseEntity.ok(chefProfileService.getChefPublicProfile(chefId));
    } catch (NoSuchElementException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @Operation(summary = "Ver detalle público de un menú", description = "Devuelve el detalle de un menú con sus platos, alérgenos (IDs de reglamento UE) y fechas ocupadas del chef. Endpoint público.")
  @GetMapping("/menus/{menuId}/public")
  public ResponseEntity<MenuPublicDetailDto> obtenerDetalleMenuPublico(@PathVariable Long menuId) {
    try {
      MenuPublicDetailDto dto = chefProfileService.getMenuPublicDetail(menuId);
      return ResponseEntity.ok(dto);
    } catch (NoSuchElementException e) {
      return ResponseEntity.notFound().build();
    }
  }

  // Authenticated profile

  @Operation(summary = "Actualizar perfil del chef", description = "Actualiza los campos del perfil profesional del chef: bio, localización, idiomas, premios y fotos.")
  @SecurityRequirement(name = "bearerAuth")
  @PatchMapping("/profile")
  public ResponseEntity<ChefPublicDetailDto> updateChefProfile(Authentication authentication,
                                                               @RequestBody ChefUReqDto dto) {
    return ResponseEntity.ok(chefProfileService.updateChefProfile(authentication, dto));
  }

  @Operation(summary = "Subir foto de perfil", description = "Sube una imagen de perfil para el chef. Se acepta JPEG, PNG o WebP con un tamaño máximo de 5 MB. Se almacena en base64.")
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/profile/photo")
  public ResponseEntity<Map<String, String>> uploadPhoto(
    @RequestParam("file") MultipartFile file,
    Authentication authentication
  ) {
    String base64 = photoUploadService.uploadChefPhoto(file, authentication);
    return ResponseEntity.ok(Map.of("photo", base64));
  }

  @Operation(summary = "Subir foto de portada", description = "Sube una imagen de portada para el perfil del chef. Mismas restricciones que la foto de perfil: imagen, máximo 5 MB.")
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/profile/cover-photo")
  public ResponseEntity<Map<String, String>> uploadCoverPhoto(@RequestParam("file") MultipartFile file,
                                                              Authentication authentication) {
    return ResponseEntity.ok(Map.of("coverPhoto", photoUploadService.uploadChefCoverPhoto(file, authentication)));
  }
}
