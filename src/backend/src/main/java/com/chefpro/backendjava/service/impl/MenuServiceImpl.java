package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.MenuCReqDto;
import com.chefpro.backendjava.common.object.dto.MenuDTO;
import com.chefpro.backendjava.common.object.dto.MenuUReqDto;
import com.chefpro.backendjava.common.object.dto.PlatoDto;
import com.chefpro.backendjava.common.object.entity.Plato;
import com.chefpro.backendjava.repository.MenuRepository;
import com.chefpro.backendjava.common.object.entity.Menu;
import com.chefpro.backendjava.service.MenuService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("MenuService")
public class MenuServiceImpl implements MenuService {

    private final MenuRepository repository;

    public MenuServiceImpl(MenuRepository repository) {
        this.repository = repository;
    }

    @Override
    public void crearMenu(MenuCReqDto dto, Authentication authentication) {


//llamada al repositorio para crear el menu
    }

  @Override
  public List<MenuDTO> listarPorChef(Authentication authentication) {

    List<Menu> menus = repository.findByChefUsername(authentication.getName());

    List<MenuDTO> menusDtoFinalList = menus.stream()
      .map(menu -> MenuDTO.builder()
        .id(menu.getId())
        .title(menu.getTitle())
        .description(menu.getDescription())
        .dishes(mapPlatosToDtos(menu.getDishes())) // Aquí está el cambio
        .allergens(menu.getAllergens())
        .pricePerPerson(menu.getPricePerPerson())
        .deliveryAvailable(menu.isDeliveryAvailable())
        .cookAtClientHome(menu.isCookAtClientHome())
        .pickupAvailable(menu.isPickupAvailable())
        .chefUsername(menu.getChef().getName())
        .createdAt(menu.getCreatedAt())
        .build()
      )
      .toList();

    return menusDtoFinalList;
  }

  @Override
  public void deleteMenu(Authentication authentication, Long idMenu) {

  }

  @Override
  public MenuDTO updateMenu(Authentication authentication, MenuUReqDto uReq) {
    return null;
  }

  private List<PlatoDto> mapPlatosToDtos(List<Plato> platos) {
    if (platos == null) {
      return null;
    }

    return platos.stream()
      .map(plato -> new PlatoDto(
        Long.parseLong(plato.getId()), // Convertir String a Long
        plato.getTitle(), // name
        plato.getDescription(),
        plato.getChef().getName(), // creator
        null, // vegan - no existe en Plato
        null, // vegetarian - no existe en Plato
        null, // allergies - no existe en Plato
        null  // ingredients - no existe en Plato
      ))
      .toList();
  }
}
