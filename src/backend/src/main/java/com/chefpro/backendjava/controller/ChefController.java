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
    return ResponseEntity.status(200).build();
  }

  @DeleteMapping("/menus")
  public ResponseEntity<MenuDTO> deleteMenu(Authentication authentication, @RequestBody Long idMenu) {


    menuService.deleteMenu(authentication, idMenu);

    return ResponseEntity.status(200).build();
  }

  @PatchMapping("/menus")
  public MenuDTO patchMenu(Authentication authentication, @RequestBody MenuUReqDto menuUpdateDto) {

    return menuService.updateMenu(authentication, menuUpdateDto);
  }

  @GetMapping("/plato")
  public List<PlatoDto> getPlato(Authentication authentication, @RequestParam(required = false) String nombrePlato) {


    return platoService.getPlatos(authentication, nombrePlato);
  }

  @PostMapping("/plato")
  public ResponseEntity<PlatoDto> createPlato(Authentication authentication, @RequestBody PlatoCReqDto platoCreateRequest) {


    platoService.createPlato(authentication, platoCreateRequest);

    return ResponseEntity.status(200).build();
  }

  @DeleteMapping("/plato")
  public ResponseEntity<PlatoDto> deletePlato(Authentication authentication, @RequestBody Long idPlato) {


    platoService.deletePlato(authentication, idPlato);

    return ResponseEntity.status(200).build();
  }

  @PatchMapping("/plato")
  public PlatoDto patchPlato(Authentication authentication, @RequestBody PlatoUReqDto platoUpdateRequest) {

    return platoService.updatePlato(authentication, platoUpdateRequest);
  }
}
