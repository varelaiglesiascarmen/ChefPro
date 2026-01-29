package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.PlatoDto;
import com.chefpro.backendjava.common.object.dto.PlatoCReqDto;
import com.chefpro.backendjava.common.object.dto.PlatoUReqDto;
import com.chefpro.backendjava.service.PlatoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component("platoService")
public class PlatoServiceImpl implements PlatoService {


  @Override
  @Transactional
  public void createPlato(Authentication auth, PlatoCReqDto cReq) {

    //Llamada al repository para crear el plato
  }

  @Override
  public List<PlatoDto> getPlatos(Authentication authentication, String nombrePlato) {

    if(nombrePlato != null && !nombrePlato.isEmpty()){
      //llamada al repositorio pasandole de parametro nombre plato
    }

    //llamada al repo sin parametros, devuelve todo lo del CHEF
    return List.of();
  }

  @Override
  @Transactional
  public void deletePlato(Authentication authentication, Long idPlato) {
    //primero, llamada al repository para buscar todos los platos del chef y ver si el id que se pasa para borrar existe
    //Segundo, borramos ese plato
  }

  @Override
  @Transactional
  public PlatoDto updatePlato(Authentication authentication, PlatoUReqDto uReq) {

    //primero, extraemos el id de uReq y buscamos si existe ese plato
    //Si existe, sustituimos sus datos por los nuevos de uReq
    //Volvemos a chutarselo a base de datos.
    return null;
  }
}
