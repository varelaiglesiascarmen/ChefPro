package com.chefpro.backendjava.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.chefpro.backendjava.common.object.dto.MenuCReqDto;
import com.chefpro.backendjava.common.object.dto.MenuDTO;
import com.chefpro.backendjava.common.object.dto.MenuUReqDto;

public interface MenuService {

  MenuDTO createMenu (MenuCReqDto dto, Authentication authentication);

  List<MenuDTO> listByChef(Authentication authentication);

  void deleteMenu(Authentication authentication, Long idMenu);

  MenuDTO updateMenu (Authentication authentication, MenuUReqDto uReq);

  List<MenuDTO> listAllMenus();
}
