package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.ChefPublicDetailDto;
import com.chefpro.backendjava.common.object.dto.MenuPublicDetailDto;

public interface ChefProfileService {

  ChefPublicDetailDto getChefPublicProfile(Long chefId);

  MenuPublicDetailDto getMenuPublicDetail(Long menuId);
}
