package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.DishDto;
import com.chefpro.backendjava.common.object.dto.MenuCReqDto;
import com.chefpro.backendjava.common.object.dto.MenuDTO;
import com.chefpro.backendjava.common.object.dto.MenuUReqDto;
import com.chefpro.backendjava.common.object.entity.AllergenDish;
import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.common.object.entity.Menu;
import com.chefpro.backendjava.repository.ChefRepository;
import com.chefpro.backendjava.repository.MenuRepository;
import com.chefpro.backendjava.service.MenuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("menuService")
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

    System.out.println("AUTH NAME = " + authentication.getName());

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
  @Transactional(readOnly = true)
  public List<MenuDTO> listByChef(Authentication authentication) {

    Chef chef = chefRepository.findByUser_Username(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Chef not found"));

    List<Menu> menus = menuRepository.findByChefIdWithDishes(chef.getId());

    // Cargar alérgenos en una segunda query (Hibernate lo hace automáticamente)
    menus.forEach(menu ->
      menu.getDishes().forEach(dish ->
        dish.getAllergenDishes().size() // Forzar carga lazy
      )
    );

    return menus.stream()
      .map(this::convertToDto)
      .toList();
  }

  private MenuDTO convertToDto(Menu menu) {
    List<DishDto> dishes = menu.getDishes().stream()
      .map(dish -> {
        List<String> allergens = dish.getAllergenDishes().stream()
          .map(AllergenDish::getAllergen)
          .collect(Collectors.toList());

        String creatorName = menu.getChef().getUser().getName() != null && menu.getChef().getUser().getLastname() != null
          ? menu.getChef().getUser().getName() + " " + menu.getChef().getUser().getLastname()
          : menu.getChef().getUser().getUsername();

        return DishDto.builder()
          .menuId(dish.getMenuId())
          .dishId(dish.getDishId())
          .title(dish.getTitle())
          .description(dish.getDescription())
          .category(dish.getCategory())
          .creator(creatorName)
          .allergens(allergens)
          .build();
      })
      .collect(Collectors.toList());

    Set<String> allergens = menu.getDishes().stream()
      .flatMap(dish -> dish.getAllergenDishes().stream())
      .map(AllergenDish::getAllergen)
      .collect(Collectors.toSet());

    return MenuDTO.builder()
      .id(menu.getId())
      .title(menu.getTitle())
      .description(menu.getDescription())
      .pricePerPerson(menu.getPricePerPerson())
      .dishes(dishes)
      .allergens(allergens)
      .chefUsername(menu.getChef().getUser().getUsername())
      .createdAt(null)
      .build();
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

      .chefUsername(authentication.getName())
      .createdAt(null)
      .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<MenuDTO> listAllMenus(
    String title,
    String description,
    String chefUsername) {

    // Obtener menús con filtros aplicados
    List<Menu> menus = menuRepository.findAllWithDishesAndFilters(title, description, chefUsername);

    // Si no hay resultados con los filtros, obtener todos los menús
    if (menus.isEmpty()) {
      menus = menuRepository.findAllWithDishes();
    }

    // Cargar alérgenos (forzar carga lazy)
    menus.forEach(menu ->
      menu.getDishes().forEach(dish ->
        dish.getAllergenDishes().size()
      )
    );

    return menus.stream()
      .map(this::convertToDto)
      .toList();
  }

}
