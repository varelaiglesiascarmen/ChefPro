package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.service.MenuService;
import com.chefpro.backendjava.service.PlatoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chef")
public class ChefController {

  private final MenuService menuService;
  private final PlatoService platoService;

  public ChefController(MenuService menuService, PlatoService platoService) {
    this.menuService = menuService;
    this.platoService = platoService;
  }

  @GetMapping("/menus")
  public List<MenuDTO> getMenusDelChef(Authentication authentication) {

    return menuService.listByChef(authentication);
  }


  @PostMapping("/menus")
  public ResponseEntity<MenuDTO> crearMenu(@RequestBody MenuCReqDto menuDto, Authentication authentication) {


    menuService.createMenu(menuDto, authentication);
    return ResponseEntity.status(201).build();
  }

  //TODO se podría pasar el parametro por url @DeleteMapping("/menus/{id}")
  @DeleteMapping("/menus")
  public ResponseEntity<MenuDTO> deleteMenu(Authentication authentication, @RequestBody Long idMenu) {


    menuService.deleteMenu(authentication, idMenu);

    return ResponseEntity.status(204).build();
  }

  @PatchMapping("/menus")
  public MenuDTO patchMenu(Authentication authentication, @RequestBody MenuUReqDto menuUpdateDto) {

    return menuService.updateMenu(authentication, menuUpdateDto);
  }

  @GetMapping("/plato")
  public List<DishDto> getPlato(Authentication authentication, @RequestParam(required = false) String nombrePlato) {


    return platoService.getDish(authentication, nombrePlato);
  }

  @PostMapping("/plato")
  public ResponseEntity<DishDto> createPlato(Authentication authentication, @RequestBody DishCReqDto platoCreateRequest) {


    platoService.createDish(authentication, platoCreateRequest);

    return ResponseEntity.status(201).build();
  }

  @DeleteMapping("/plato")
  public ResponseEntity<Void> deletePlato(Authentication authentication, @RequestParam Long menuId, @RequestParam Long dishId
  ) {
    platoService.deleteDish(authentication, menuId, dishId);
    return ResponseEntity.status(204).build();
  }

  @PatchMapping("/plato")
  public ResponseEntity<DishDto> patchPlato(Authentication authentication, @RequestBody DishUReqDto platoUpdateRequest) {
    DishDto updatedDish = platoService.updateDish(authentication, platoUpdateRequest);
    return ResponseEntity.ok(updatedDish);
  }
}
