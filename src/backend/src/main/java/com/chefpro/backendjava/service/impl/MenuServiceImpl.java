package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.MenuCReqDto;
import com.chefpro.backendjava.common.object.dto.MenuDTO;
import com.chefpro.backendjava.common.object.dto.MenuUReqDto;
import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.common.object.entity.Menu;
import com.chefpro.backendjava.repository.ChefRepository;
import com.chefpro.backendjava.repository.MenuRepository;
import com.chefpro.backendjava.service.MenuService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component("MenuService")
public class MenuServiceImpl implements MenuService {

  private final MenuRepository menuRepository;
  private final ChefRepository chefRepository;

  public MenuServiceImpl(MenuRepository menuRepository,
                         ChefRepository chefRepository) {
    this.menuRepository = menuRepository;
    this.chefRepository = chefRepository;
  }

  @Override
  @Transactional
  public void createMenu(MenuCReqDto dto, Authentication authentication) {

    Chef chef = chefRepository.findByUser_Username(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Authenticated chef not found"));

    if (dto.getTitle() == null || dto.getTitle().isBlank()) {
      throw new IllegalArgumentException("title is required");
    }

    if (dto.getPricePerPerson() == null || dto.getPricePerPerson().signum() <= 0) {
      throw new IllegalArgumentException("pricePerPerson must be > 0");
    }

    Menu menu = Menu.builder()
      .title(dto.getTitle())
      .description(dto.getDescription())
      .pricePerPerson(dto.getPricePerPerson())
      .chef(chef)
      .minNumberDiners(dto.getMinNumberDiners())
      .maxNumberDiners(dto.getMaxNumberDiners())
      .kitchenRequirements(dto.getKitchenRequirements())
      .build();

    menuRepository.save(menu);
  }

  @Override
  public List<MenuDTO> listByChef(Authentication authentication) {

    List<Menu> menus =
      menuRepository.findByChef_User_Username(authentication.getName());

    return menus.stream()
      .map(menu -> MenuDTO.builder()
        .id(menu.getId())
        .title(menu.getTitle())
        .description(menu.getDescription())
        .pricePerPerson(menu.getPricePerPerson())

        .dishes(List.of())
        .allergens(Set.of())
        .deliveryAvailable(false)
        .cookAtClientHome(false)
        .pickupAvailable(false)

        .chefUsername(authentication.getName())
        .createdAt(null)
        .build()
      )
      .toList();
  }

  @Override
  @Transactional
  public void deleteMenu(Authentication authentication, Long idMenu) {

    Chef chef = chefRepository.findByUser_Username(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Authenticated chef not found"));

    Menu menu = menuRepository.findById(idMenu)
      .orElseThrow(() -> new RuntimeException("Menu not found: " + idMenu));

    if (!chef.getId().equals(menu.getChef().getId())) {
      throw new RuntimeException("Not allowed to delete this menu");
    }

    menuRepository.delete(menu);
  }

  @Override
  @Transactional
  public MenuDTO updateMenu(Authentication authentication, MenuUReqDto uReq) {

    Chef chef = chefRepository.findByUser_Username(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Authenticated chef not found"));

    if (uReq.getId() == null) {
      throw new IllegalArgumentException("id is required");
    }

    Menu menu = menuRepository.findById(uReq.getId())
      .orElseThrow(() -> new RuntimeException("Menu not found: " + uReq.getId()));

    if (!chef.getId().equals(menu.getChef().getId())) {
      throw new RuntimeException("Not allowed to update this menu");
    }

    if (uReq.getTitle() != null && !uReq.getTitle().isBlank()) {
      menu.setTitle(uReq.getTitle());
    }

    if (uReq.getDescription() != null) {
      menu.setDescription(uReq.getDescription());
    }

    if (uReq.getPricePerPerson() != null && uReq.getPricePerPerson().signum() > 0) {
      menu.setPricePerPerson(uReq.getPricePerPerson());
    }


    Menu saved = menuRepository.save(menu);

    return MenuDTO.builder()
      .id(saved.getId())
      .title(saved.getTitle())
      .description(saved.getDescription())
      .pricePerPerson(saved.getPricePerPerson())

      .dishes(List.of())
      .allergens(Set.of())
      .deliveryAvailable(false)
      .cookAtClientHome(false)
      .pickupAvailable(false)

      .chefUsername(authentication.getName())
      .createdAt(null)
      .build();
  }

}
