package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.PlatoDto;
import com.chefpro.backendjava.common.object.dto.PlatoCReqDto;
import com.chefpro.backendjava.common.object.dto.PlatoUReqDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PlatoService {

    void createPlato (Authentication auth, PlatoCReqDto cReq);

    List<PlatoDto> getPlatos(Authentication authentication, String nombrePlato);

    void deletePlato(Authentication authentication, Long idPlato);

    PlatoDto updatePlato (Authentication authentication, PlatoUReqDto uReq);


}
