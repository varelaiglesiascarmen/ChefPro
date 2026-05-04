package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.DishDto;
import com.chefpro.backendjava.common.object.dto.DishCReqDto;
import com.chefpro.backendjava.common.object.dto.DishUReqDto;
import com.chefpro.backendjava.common.object.entity.*;
import com.chefpro.backendjava.common.util.ChefResolver;
import com.chefpro.backendjava.repository.*;
import com.chefpro.backendjava.service.DishService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("dishService")
public class DishServiceImpl implements DishService {

  private final ChefResolver chefResolver;
  private final MenuRepository menuRepository;
  private final DishRepository dishRepository;
  private final AllergenDishRepository allergenDishRepository;
  private final OfficialAllergenRepository officialAllergenRepository;

  public DishServiceImpl(ChefResolver chefResolver,
                         MenuRepository menuRepository,
                         DishRepository dishRepository,
                         AllergenDishRepository allergenDishRepository,
                         OfficialAllergenRepository officialAllergenRepository) {
    this.chefResolver = chefResolver;
    this.menuRepository = menuRepository;
    this.dishRepository = dishRepository;
    this.allergenDishRepository = allergenDishRepository;
    this.officialAllergenRepository = officialAllergenRepository;
  }

  @Override
  @Transactional
  public void createDish(Authentication authentication, DishCReqDto cReq) {
    Chef chef = chefResolver.resolve(authentication);

    Menu menu = menuRepository.findById(cReq.getMenuId())
      .orElseThrow(() -> new IllegalArgumentException("Menu not found"));

    if (!menu.getChef().getId().equals(chef.getId())) {
      throw new IllegalArgumentException("This menu does not belong to the authenticated chef");
    }
    if (cReq.getTitle() == null || cReq.getTitle().isBlank()) {
      throw new IllegalArgumentException("Title is required");
    }

    Long nextDishId = dishRepository.findMaxDishIdByMenuId(cReq.getMenuId())
      .map(maxId -> maxId + 1)
      .orElse(1L);

    Dish dish = Dish.builder()
      .menuId(cReq.getMenuId())
      .dishId(nextDishId)
      .title(cReq.getTitle())
      .description(cReq.getDescription())
      .category(cReq.getCategory())
      .photo(cReq.getPhoto())
      .menu(menu)
      .allergenDishes(new ArrayList<>())
      .build();

    dishRepository.save(dish);
    saveAllergens(dish, cReq.getAllergens());
  }

  @Override
  @Transactional(readOnly = true)
  public List<DishDto> getDish(Authentication authentication, String dishName) {
    Chef chef = chefResolver.resolve(authentication);

    List<Dish> dishes = (dishName != null && !dishName.isEmpty())
      ? dishRepository.findByChefIdAndTitleContaining(chef.getId(), dishName)
      : dishRepository.findAllByChefId(chef.getId());

    return dishes.stream()
      .map(dish -> toDto(dish, chef))
      .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteDish(Authentication authentication, Long menuId, Long dishId) {
    Chef chef = chefResolver.resolve(authentication);

    Dish dish = dishRepository.findById(new Dish.DishId(menuId, dishId))
      .orElseThrow(() -> new IllegalArgumentException("Dish not found"));

    if (!dish.getMenu().getChef().getId().equals(chef.getId())) {
      throw new IllegalArgumentException("This dish does not belong to the authenticated chef");
    }

    dishRepository.delete(dish);
  }

  @Override
  @Transactional
  public DishDto updateDish(Authentication authentication, DishUReqDto uReq) {
    Chef chef = chefResolver.resolve(authentication);

    if (uReq.getMenuId() == null || uReq.getDishId() == null) {
      throw new IllegalArgumentException("menuId and dishId are required");
    }

    Dish dish = dishRepository.findById(new Dish.DishId(uReq.getMenuId(), uReq.getDishId()))
      .orElseThrow(() -> new IllegalArgumentException("Dish not found"));

    if (!dish.getMenu().getChef().getId().equals(chef.getId())) {
      throw new IllegalArgumentException("This dish does not belong to the authenticated chef");
    }

    if (uReq.getTitle() != null && !uReq.getTitle().isBlank()) dish.setTitle(uReq.getTitle());
    if (uReq.getDescription() != null)                          dish.setDescription(uReq.getDescription());
    if (uReq.getCategory() != null)                             dish.setCategory(uReq.getCategory());
    if (uReq.getPhoto() != null)                                dish.setPhoto(uReq.getPhoto());

    if (uReq.getAllergens() != null) {
      allergenDishRepository.deleteAll(dish.getAllergenDishes());
      dish.getAllergenDishes().clear();
      saveAllergens(dish, uReq.getAllergens());
    }

    dishRepository.save(dish);
    return toDto(dish, chef);
  }

  private void saveAllergens(Dish dish, List<String> allergenNames) {
    if (allergenNames == null || allergenNames.isEmpty()) return;
    for (String allergenName : allergenNames) {
      OfficialAllergen official = officialAllergenRepository.findById(allergenName)
        .orElseThrow(() -> new IllegalArgumentException("Allergen not found: " + allergenName));
      allergenDishRepository.save(AllergenDish.builder()
        .menuId(dish.getMenuId())
        .dishId(dish.getDishId())
        .allergen(allergenName)
        .dish(dish)
        .officialAllergen(official)
        .build());
    }
  }

  private DishDto toDto(Dish dish, Chef chef) {
    String creatorName = chef.getUser().getName() != null && chef.getUser().getLastname() != null
      ? chef.getUser().getName() + " " + chef.getUser().getLastname()
      : chef.getUser().getUsername();

    return DishDto.builder()
      .menuId(dish.getMenuId())
      .dishId(dish.getDishId())
      .title(dish.getTitle())
      .description(dish.getDescription())
      .category(dish.getCategory())
      .photo(dish.getPhoto())
      .creator(creatorName)
      .allergens(dish.getAllergenDishes().stream()
        .map(AllergenDish::getAllergen)
        .collect(Collectors.toList()))
      .build();
  }
}
