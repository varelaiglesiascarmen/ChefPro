package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.ChefPublicDetailDto;
import com.chefpro.backendjava.common.object.dto.ChefUReqDto;
import com.chefpro.backendjava.common.object.dto.MenuPublicDetailDto;
import org.springframework.security.core.Authentication;

public interface ChefProfileService {

  ChefPublicDetailDto getChefPublicProfile(Long chefId);

  MenuPublicDetailDto getMenuPublicDetail(Long menuId);

  ChefPublicDetailDto updateChefProfile(Authentication authentication, ChefUReqDto dto);
}
