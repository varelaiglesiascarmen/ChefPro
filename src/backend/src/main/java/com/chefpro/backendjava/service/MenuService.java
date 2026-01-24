package com.chefpro.backendjava.service;

import com.chefpro.backendjava.controller.dto.MenuDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface MenuService {

    void crearMenu (MenuDTO dto, Authentication authentication);

    List<MenuDTO> listarPorChef(Authentication authentication);
}
