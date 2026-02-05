package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.service.ChefSearchService;
import com.chefpro.backendjava.service.MenuService;
import com.chefpro.backendjava.service.DishService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chef")
public class ChefController {

  private final MenuService menuService;
  private final DishService dishService;
  private final ChefSearchService chefSearchService;

  public ChefController(
    MenuService menuService,
    DishService dishService,
    ChefSearchService chefSearchService
  ) {
    this.menuService = menuService;
    this.dishService = dishService;
    this.chefSearchService = chefSearchService;
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
  public ResponseEntity<List<MenuDTO>> getAllMenusPublic() {
    List<MenuDTO> menus = menuService.listAllMenus();
    return ResponseEntity.ok(menus);
  }

  @GetMapping("/search")
  public ResponseEntity<Page<ChefSearchDto>> searchChefs(
    @RequestParam(required = false, name = "q") String q,
    @RequestParam(required = false, name = "date") String date,
    @RequestParam(required = false, name = "min") Integer minPrice,
    @RequestParam(required = false, name = "max") Integer maxPrice,
    @RequestParam(required = false, name = "guests") Integer guests,
    @RequestParam(required = false, name = "diets") String diets,
    @RequestParam(required = false, name = "top", defaultValue = "false") boolean top,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    Page<ChefSearchDto> result = chefSearchService.search(
      q,
      date,
      minPrice,
      maxPrice,
      guests,
      diets,
      top,
      PageRequest.of(page, size)
    );

    return ResponseEntity.ok(result);
  }
}
