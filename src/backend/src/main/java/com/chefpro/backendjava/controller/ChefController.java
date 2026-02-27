package com.chefpro.backendjava.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chefpro.backendjava.common.object.dto.ChefPublicDetailDto;
import com.chefpro.backendjava.common.object.dto.ChefSearchResultDto;
import com.chefpro.backendjava.common.object.dto.ChefUReqDto;
import com.chefpro.backendjava.common.object.dto.DishCReqDto;
import com.chefpro.backendjava.common.object.dto.DishDto;
import com.chefpro.backendjava.common.object.dto.DishUReqDto;
import com.chefpro.backendjava.common.object.dto.MenuCReqDto;
import com.chefpro.backendjava.common.object.dto.MenuDTO;
import com.chefpro.backendjava.common.object.dto.MenuPublicDetailDto;
import com.chefpro.backendjava.common.object.dto.MenuUReqDto;
import com.chefpro.backendjava.service.ChefProfileService;
import com.chefpro.backendjava.service.ChefSearchService;
import com.chefpro.backendjava.service.DishService;
import com.chefpro.backendjava.service.MenuService;

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

  @GetMapping("/menus")
  public List<MenuDTO> getMenusDelChef(Authentication authentication) {
    return menuService.listByChef(authentication);
  }

  @PostMapping("/menus")
  public ResponseEntity<MenuDTO> crearMenu(
    @RequestBody MenuCReqDto menuDto,
    Authentication authentication
  ) {
    MenuDTO createdMenu = menuService.createMenu(menuDto, authentication);
    return ResponseEntity.status(201).body(createdMenu);
  }

  @DeleteMapping("/menus/{id}")
  public ResponseEntity<Void> deleteMenu(
    Authentication authentication,
    @PathVariable Long id
  ) {
    menuService.deleteMenu(authentication, id);
    return ResponseEntity.noContent().build();
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
    return ResponseEntity.noContent().build();
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
  public ResponseEntity<List<MenuDTO>> getAllMenusPublic() {
    List<MenuDTO> menus = menuService.listAllMenus();
    return ResponseEntity.ok(menus);
  }

  @GetMapping("/search")
  public ResponseEntity<ChefSearchResultDto> searchChefs(
    @RequestParam(required = false) String q,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
    @RequestParam(required = false) BigDecimal minPrice,
    @RequestParam(required = false) BigDecimal maxPrice,
    @RequestParam(required = false) Integer guests,
    @RequestParam(required = false) List<String> allergens
  ) {
    ChefSearchResultDto result = chefSearchService.search(
      q, date, minPrice, maxPrice, guests, allergens
    );
    return ResponseEntity.ok(result);
  }

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

  @PatchMapping("/profile")
  public ResponseEntity<ChefPublicDetailDto> updateChefProfile(
    Authentication authentication,
    @RequestBody ChefUReqDto chefUpdateDto
  ) {
    ChefPublicDetailDto updated = chefProfileService.updateChefProfile(authentication, chefUpdateDto);
    return ResponseEntity.ok(updated);
  }
}
