package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.DishDto;
import com.chefpro.backendjava.common.object.dto.DishCReqDto;
import com.chefpro.backendjava.common.object.dto.DishUReqDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PlatoService {


  //TODO no olvidar que el eespañol no existe, siempre ingles, TODO INGLES
    void createDish(Authentication auth, DishCReqDto cReq);

    List<DishDto> getDish(Authentication authentication, String nombrePlato);

    void deleteDish(Authentication authentication, Long idMenu, Long idPlato);

    DishDto updateDish(Authentication authentication, DishUReqDto uReq);


}
