package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.service.ChefProfileService;
import com.chefpro.backendjava.service.ChefSearchService;
import com.chefpro.backendjava.service.DishService;
import com.chefpro.backendjava.service.MenuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/chef")
public class ChefController {

  private final MenuService menuService;
  private final DishService dishService;
  private final ChefSearchService chefSearchService;
  private final ChefProfileService chefProfileService;

  public ChefController(
    MenuService menuService,
    DishService dishService,
    ChefSearchService chefSearchService,
    ChefProfileService chefProfileService
  ) {
    this.menuService = menuService;
    this.dishService = dishService;
    this.chefSearchService = chefSearchService;
    this.chefProfileService = chefProfileService;
  }

  @GetMapping("/chef/search")
  public ResponseEntity<Page<ChefSearchDto>> searchChefs(
    @RequestParam(required = false) String name,
    @RequestParam(required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
    @PageableDefault(size = 10, sort = "id") Pageable pageable) {

    Page<ChefSearchDto> result = chefSearchService.search(name, date, pageable);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/menus")
  public List<MenuDTO> getMenusDelChef(Authentication authentication) {
    return menuService.listByChef(authentication);
  }

  @PostMapping("/menus")
  public ResponseEntity<MenuDTO> crearMenu(
    @RequestBody MenuCReqDto menuDto,
    Authentication authentication
  ) {
    menuService.createMenu(menuDto, authentication);
    return ResponseEntity.status(201).build();
  }

  @DeleteMapping("/menus/{id}")
  public ResponseEntity<Void> deleteMenu(
    Authentication authentication,
    @PathVariable Long id
  ) {
    menuService.deleteMenu(authentication, id);
    return ResponseEntity.noContent().build(); // 204
  }

  @PatchMapping("/menus")
  public MenuDTO patchMenu(
    Authentication authentication,
    @RequestBody MenuUReqDto menuUpdateDto
  ) {
    return menuService.updateMenu(authentication, menuUpdateDto);
  }

  @GetMapping("/plato")
  public List<DishDto> getPlato(
    Authentication authentication,
    @RequestParam(required = false) String nombrePlato
  ) {
    return dishService.getDish(authentication, nombrePlato);
  }

  @PostMapping("/plato")
  public ResponseEntity<DishDto> createPlato(
    Authentication authentication,
    @RequestBody DishCReqDto platoCreateRequest
  ) {
    dishService.createDish(authentication, platoCreateRequest);
    return ResponseEntity.status(201).build();
  }

  @DeleteMapping("/plato")
  public ResponseEntity<Void> deletePlato(
    Authentication authentication,
    @RequestParam Long menuId,
    @RequestParam Long dishId
  ) {
    dishService.deleteDish(authentication, menuId, dishId);
    return ResponseEntity.noContent().build(); // 204
  }

  @PatchMapping("/plato")
  public ResponseEntity<DishDto> patchPlato(
    Authentication authentication,
    @RequestBody DishUReqDto platoUpdateRequest
  ) {
    DishDto updatedDish = dishService.updateDish(authentication, platoUpdateRequest);
    return ResponseEntity.ok(updatedDish);
  }

  @GetMapping("/menus/public")
  public ResponseEntity<List<MenuDTO>> getAllMenusPublic(@RequestParam(required = false) String title,
                                                         @RequestParam (required = false) String description,
                                                         @RequestParam(required = false) Boolean pickUpAvailable,
                                                         @RequestParam(required = false) String chefUsername,
                                                         @RequestParam(required = false) Boolean deliveryAvailable,
                                                         @RequestParam(required = false) Boolean cookAtClientHome,
                                                         @PageableDefault(size = 10, sort = "id") Pageable pageable) {
    List<MenuDTO> menus = menuService.listAllMenus(title, description, pickUpAvailable, chefUsername, deliveryAvailable, cookAtClientHome);
    return ResponseEntity.ok(menus);
  }

  @GetMapping("/search")
  public ResponseEntity<Page<ChefSearchDto>> searchChefs(
    @RequestParam(required = false) String name,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
    @PageableDefault(size = 10, sort = "id") Pageable pageable) {

    Page<ChefSearchDto> result = chefSearchService.search(name, date, pageable);

    return ResponseEntity.ok(result);
  }

  // ========================================
  // ENDPOINTS PÚBLICOS - Perfil de Chef y Menú
  // ========================================

  @GetMapping("/{chefId}/profile")
  public ResponseEntity<ChefPublicDetailDto> getChefPublicProfile(@PathVariable Long chefId) {
    try {
      ChefPublicDetailDto dto = chefProfileService.getChefPublicProfile(chefId);
      return ResponseEntity.ok(dto);
    } catch (NoSuchElementException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/menus/{menuId}/public")
  public ResponseEntity<MenuPublicDetailDto> getMenuPublicDetail(@PathVariable Long menuId) {
    try {
      MenuPublicDetailDto dto = chefProfileService.getMenuPublicDetail(menuId);
      return ResponseEntity.ok(dto);
    } catch (NoSuchElementException e) {
      return ResponseEntity.notFound().build();
    }
  }

  // ========================================
  // AUTHENTICATED ENDPOINT - Update Chef Profile
  // ========================================

  @PatchMapping("/profile")
  public ResponseEntity<ChefPublicDetailDto> updateChefProfile(
    Authentication authentication,
    @RequestBody ChefUReqDto chefUpdateDto
  ) {
    ChefPublicDetailDto updated = chefProfileService.updateChefProfile(authentication, chefUpdateDto);
    return ResponseEntity.ok(updated);
  }
}
