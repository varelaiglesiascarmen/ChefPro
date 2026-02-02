package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.*;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface MenuService {

  void crearMenu (MenuCReqDto dto, Authentication authentication);

  List<MenuDTO> listarPorChef(Authentication authentication);

  void deleteMenu(Authentication authentication, Long idMenu);

  MenuDTO updateMenu (Authentication authentication, MenuUReqDto uReq);
}
