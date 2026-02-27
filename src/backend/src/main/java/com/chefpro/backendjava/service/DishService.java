package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.DishCReqDto;
import com.chefpro.backendjava.common.object.dto.DishDto;
import com.chefpro.backendjava.common.object.dto.DishUReqDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface DishService {

  void createDish(Authentication authentication, DishCReqDto dishCReqDto);

  List<DishDto> getDish(Authentication authentication, String dishName);

  void deleteDish(Authentication authentication, Long menuId, Long dishId);

  DishDto updateDish(Authentication authentication, DishUReqDto dishUReqDto);
}
