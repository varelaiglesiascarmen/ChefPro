package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.*;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface MenuService {

  void createMenu (MenuCReqDto dto, Authentication authentication);

  List<MenuDTO> listByChef(Authentication authentication);

  void deleteMenu(Authentication authentication, Long idMenu);

  MenuDTO updateMenu (Authentication authentication, MenuUReqDto uReq);
}
