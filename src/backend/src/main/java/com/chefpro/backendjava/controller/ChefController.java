package com.chefpro.backendjava.controller;

import com.chefpro.backendjava.entity.Menu;
import com.chefpro.backendjava.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chef")
public class ChefController {

    private final MenuService menuService;

    public ChefController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menus")
    public List<Menu> getMenusDelChef(Principal principal) {
        String chefUsername = principal.getName();
        return menuService.listarPorChef(chefUsername);
    }


    @PostMapping("/menus")
    public Menu crearMenu(@RequestBody Menu menu, Principal principal) {
        String chefUsername = principal.getName();
        return menuService.crearMenu(
                menu.getNombre(),
                menu.getDescripcion(),
                menu.getPrecio(),
                chefUsername
        );
    }
}
