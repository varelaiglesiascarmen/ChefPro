package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.service.MenuService;
import com.chefpro.backendjava.controller.dto.MenuDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chef")
public class ChefController {

    private final MenuService menuService;

    public ChefController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menus")
    public List<MenuDTO> getMenusDelChef(Authentication authentication) {

        return menuService.listarPorChef(authentication);
    }


    @PostMapping("/menus")
    public ResponseEntity<MenuDTO> crearMenu(@RequestBody MenuDTO menuDto, Authentication authentication) {

        return ResponseEntity.status(200).build();
    }
}
