package com.chefpro.backendjava.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.service.*;

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

  @GetMapping("/menus")
  public List<MenuDTO> listMenus(Authentication authentication) {
    return menuService.listByChef(authentication);
  }

  @PostMapping("/menus")
  public ResponseEntity<MenuDTO> createMenu(@RequestBody MenuCReqDto dto, Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createMenu(dto, authentication));
  }

  @DeleteMapping("/menus/{id}")
  public ResponseEntity<Void> deleteMenu(@PathVariable Long id, Authentication authentication) {
    menuService.deleteMenu(authentication, id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/menus")
  public MenuDTO updateMenu(@RequestBody MenuUReqDto dto, Authentication authentication) {
    return menuService.updateMenu(authentication, dto);
  }

  @GetMapping("/menus/public")
  public ResponseEntity<List<MenuDTO>> listPublicMenus() {
    return ResponseEntity.ok(menuService.listAllMenus());
  }

  // Dishes

  @GetMapping("/plato")
  public List<DishDto> getDishes(Authentication authentication,
                                 @RequestParam(required = false) String nombrePlato) {
    return dishService.getDish(authentication, nombrePlato);
  }

  @PostMapping("/plato")
  public ResponseEntity<Void> createDish(Authentication authentication, @RequestBody DishCReqDto dto) {
    dishService.createDish(authentication, dto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/plato")
  public ResponseEntity<Void> deleteDish(Authentication authentication,
                                         @RequestParam Long menuId,
                                         @RequestParam Long dishId) {
    dishService.deleteDish(authentication, menuId, dishId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/plato")
  public ResponseEntity<DishDto> updateDish(Authentication authentication, @RequestBody DishUReqDto dto) {
    return ResponseEntity.ok(dishService.updateDish(authentication, dto));
  }

  // Search

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

  @GetMapping("/{chefId}/profile")
  public ResponseEntity<ChefPublicDetailDto> getChefPublicProfile(@PathVariable Long chefId) {
    try {
      return ResponseEntity.ok(chefProfileService.getChefPublicProfile(chefId));
    } catch (NoSuchElementException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/menus/{menuId}/public")
  public ResponseEntity<MenuPublicDetailDto> getPublicMenuDetail(@PathVariable Long menuId) {
    try {
      return ResponseEntity.ok(chefProfileService.getMenuPublicDetail(menuId));
    } catch (NoSuchElementException e) {
      return ResponseEntity.notFound().build();
    }
  }

  // Authenticated profile

  @PatchMapping("/profile")
  public ResponseEntity<ChefPublicDetailDto> updateChefProfile(Authentication authentication,
                                                               @RequestBody ChefUReqDto dto) {
    return ResponseEntity.ok(chefProfileService.updateChefProfile(authentication, dto));
  }

  @PostMapping("/profile/photo")
  public ResponseEntity<Map<String, String>> uploadPhoto(@RequestParam("file") MultipartFile file,
                                                         Authentication authentication) {
    return ResponseEntity.ok(Map.of("photo", photoUploadService.uploadChefPhoto(file, authentication)));
  }

  @PostMapping("/profile/cover-photo")
  public ResponseEntity<Map<String, String>> uploadCoverPhoto(@RequestParam("file") MultipartFile file,
                                                              Authentication authentication) {
    return ResponseEntity.ok(Map.of("coverPhoto", photoUploadService.uploadChefCoverPhoto(file, authentication)));
  }
}
