package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface MenuService {

  void createMenu (MenuCReqDto dto, Authentication authentication);

  List<MenuDTO> listByChef(Authentication authentication);

  void deleteMenu(Authentication authentication, Long idMenu);

  MenuDTO updateMenu (Authentication authentication, MenuUReqDto uReq);

  List<MenuDTO> listAllMenus(String title, String description, Boolean pickUpAvailable, String chefUsername, Boolean deliveryAvailable, Boolean cookAtClientHome);
}
