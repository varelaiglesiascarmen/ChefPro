package com.chefpro.backendjava.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.chefpro.backendjava.common.object.dto.DishSummaryDto;
import com.chefpro.backendjava.common.object.dto.MenuCReqDto;
import com.chefpro.backendjava.common.object.dto.MenuDTO;
import com.chefpro.backendjava.common.object.dto.MenuUReqDto;
import com.chefpro.backendjava.common.object.entity.AllergenDish;
import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.common.object.entity.Menu;
import com.chefpro.backendjava.common.util.ChefResolver;
import com.chefpro.backendjava.repository.MenuRepository;
import com.chefpro.backendjava.service.MenuService;

@Component("menuService")
public class MenuServiceImpl implements MenuService {

  private final MenuRepository menuRepository;
  private final ChefResolver chefResolver;

  public MenuServiceImpl(MenuRepository menuRepository, ChefResolver chefResolver) {
    this.menuRepository = menuRepository;
    this.chefResolver = chefResolver;
  }

  @Override
  @Transactional
  public MenuDTO createMenu(MenuCReqDto dto, Authentication authentication) {
    Chef chef = chefResolver.resolve(authentication);

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

    Menu saved = menuRepository.save(menu);

    return MenuDTO.builder()
      .menuId(saved.getId())
      .title(saved.getTitle())
      .description(saved.getDescription())
      .pricePerPerson(saved.getPricePerPerson())
      .chefUsername(chef.getUser().getUsername())
      .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<MenuDTO> listByChef(Authentication authentication) {
    Chef chef = chefResolver.resolve(authentication);
    List<Menu> menus = menuRepository.findByChefIdWithDishes(chef.getId());
    menus.forEach(menu -> menu.getDishes().forEach(dish -> dish.getAllergenDishes().size()));
    return menus.stream().map(this::toDto).toList();
  }

  @Override
  @Transactional
  public void deleteMenu(Authentication authentication, Long menuId) {
    Chef chef = chefResolver.resolve(authentication);

    Menu menu = menuRepository.findById(menuId)
      .orElseThrow(() -> new RuntimeException("Menu not found: " + menuId));

    if (!chef.getId().equals(menu.getChef().getId())) {
      throw new RuntimeException("Not allowed to delete this menu");
    }
    if (menu.getReservations() != null && !menu.getReservations().isEmpty()) {
      throw new RuntimeException("Cannot delete a menu with active reservations");
    }

    menuRepository.delete(menu);
  }

  @Override
  @Transactional
  public MenuDTO updateMenu(Authentication authentication, MenuUReqDto uReq) {
    Chef chef = chefResolver.resolve(authentication);

    if (uReq.getId() == null) {
      throw new IllegalArgumentException("id is required");
    }

    Menu menu = menuRepository.findById(uReq.getId())
      .orElseThrow(() -> new RuntimeException("Menu not found: " + uReq.getId()));

    if (!chef.getId().equals(menu.getChef().getId())) {
      throw new RuntimeException("Not allowed to update this menu");
    }

    if (uReq.getTitle() != null && !uReq.getTitle().isBlank())     menu.setTitle(uReq.getTitle());
    if (uReq.getDescription() != null)                              menu.setDescription(uReq.getDescription());
    if (uReq.getPricePerPerson() != null
        && uReq.getPricePerPerson().signum() > 0)                   menu.setPricePerPerson(uReq.getPricePerPerson());
    if (uReq.getMinNumberDiners() != null
        && uReq.getMinNumberDiners() > 0)                           menu.setMinNumberDiners(uReq.getMinNumberDiners());
    if (uReq.getMaxNumberDiners() != null
        && uReq.getMaxNumberDiners() > 0)                           menu.setMaxNumberDiners(uReq.getMaxNumberDiners());
    if (uReq.getKitchenRequirements() != null)                      menu.setKitchenRequirements(uReq.getKitchenRequirements());

    Menu saved = menuRepository.save(menu);

    return MenuDTO.builder()
      .menuId(saved.getId())
      .title(saved.getTitle())
      .description(saved.getDescription())
      .pricePerPerson(saved.getPricePerPerson())
      .minNumberDiners(saved.getMinNumberDiners())
      .maxNumberDiners(saved.getMaxNumberDiners())
      .kitchenRequirements(saved.getKitchenRequirements())
      .dishes(List.of())
      .allergens(Set.of())
      .deliveryAvailable(false)
      .cookAtClientHome(false)
      .pickupAvailable(false)
      .chefUsername(authentication.getName())
      .createdAt(null)
      .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<MenuDTO> listAllMenus() {
    List<Menu> menus = menuRepository.findAllWithDishes();
    menus.forEach(menu -> menu.getDishes().forEach(dish -> dish.getAllergenDishes().size()));
    return menus.stream().map(this::toDto).toList();
  }

  private MenuDTO toDto(Menu menu) {
    List<DishSummaryDto> dishes = menu.getDishes().stream()
      .map(dish -> DishSummaryDto.builder()
        .menuId(dish.getMenuId())
        .dishId(dish.getDishId())
        .title(dish.getTitle())
        .description(dish.getDescription())
        .category(dish.getCategory())
        .allergens(dish.getAllergenDishes().stream()
          .map(AllergenDish::getAllergen)
          .collect(Collectors.toList()))
        .photo(dish.getPhoto())
        .build())
      .collect(Collectors.toList());

    Set<String> allergens = menu.getDishes().stream()
      .flatMap(dish -> dish.getAllergenDishes().stream())
      .map(AllergenDish::getAllergen)
      .collect(Collectors.toSet());

    return MenuDTO.builder()
      .menuId(menu.getId())
      .title(menu.getTitle())
      .description(menu.getDescription())
      .pricePerPerson(menu.getPricePerPerson())
      .minNumberDiners(menu.getMinNumberDiners())
      .maxNumberDiners(menu.getMaxNumberDiners())
      .kitchenRequirements(menu.getKitchenRequirements())
      .dishes(dishes)
      .allergens(allergens)
      .deliveryAvailable(false)
      .cookAtClientHome(false)
      .pickupAvailable(false)
      .chefUsername(menu.getChef().getUser().getUsername())
      .createdAt(null)
      .build();
  }
}
