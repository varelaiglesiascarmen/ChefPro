package com.chefpro.service.impl;

import com.chefpro.backendjava.common.object.dto.DishCReqDto;
import com.chefpro.backendjava.common.object.dto.DishDto;
import com.chefpro.backendjava.common.object.dto.DishUReqDto;
import com.chefpro.backendjava.common.object.entity.*;
import com.chefpro.backendjava.repository.*;
import com.chefpro.backendjava.service.DishService;
import com.chefpro.backendjava.service.impl.DishServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DishServiceTest {

  private DishService dishService;

  private ChefRepository chefRepository;
  private MenuRepository menuRepository;
  private DishRepository dishRepository;
  private AllergenDishRepository allergenDishRepository;
  private OfficialAllergenRepository officialAllergenRepository;
  private Authentication authentication;

  private Chef chef;
  private UserLogin userLogin;
  private Menu menu;

  @BeforeEach
  void setUp() {
    chefRepository            = mock(ChefRepository.class);
    menuRepository            = mock(MenuRepository.class);
    dishRepository            = mock(DishRepository.class);
    allergenDishRepository    = mock(AllergenDishRepository.class);
    officialAllergenRepository = mock(OfficialAllergenRepository.class);
    authentication            = mock(Authentication.class);

    dishService = new DishServiceImpl(
      chefRepository, menuRepository, dishRepository,
      allergenDishRepository, officialAllergenRepository
    );

    userLogin = mock(UserLogin.class);
    when(userLogin.getName()).thenReturn("Mario");
    when(userLogin.getLastname()).thenReturn("Rossi");
    when(userLogin.getUsername()).thenReturn("mario@example.com");

    chef = mock(Chef.class);
    when(chef.getId()).thenReturn(1L);
    when(chef.getUser()).thenReturn(userLogin);

    menu = mock(Menu.class);
    when(menu.getId()).thenReturn(10L);
    when(menu.getChef()).thenReturn(chef);

    when(authentication.getName()).thenReturn("mario@example.com");
    when(chefRepository.findByUser_Username("mario@example.com")).thenReturn(Optional.of(chef));
  }

  // ─── createDish ──────────────────────────────────────────────────────────

  @Test
  void createDish_success_savesEntity() {
    DishCReqDto req = mock(DishCReqDto.class);
    when(req.getMenuId()).thenReturn(10L);
    when(req.getTitle()).thenReturn("Pasta Carbonara");
    when(req.getDescription()).thenReturn("Descripción");
    when(req.getCategory()).thenReturn("Primero");
    when(req.getPhoto()).thenReturn(null);
    when(req.getAllergens()).thenReturn(new ArrayList<>());

    when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));
    when(dishRepository.findMaxDishIdByMenuId(10L)).thenReturn(Optional.of(3L));

    dishService.createDish(authentication, req);

    verify(dishRepository).save(any(Dish.class));
    verifyNoInteractions(allergenDishRepository);
  }

  @Test
  void createDish_withAllergens_savesAllergenDishes() {
    DishCReqDto req = mock(DishCReqDto.class);
    when(req.getMenuId()).thenReturn(10L);
    when(req.getTitle()).thenReturn("Pasta Carbonara");
    when(req.getDescription()).thenReturn("Descripcion");
    when(req.getCategory()).thenReturn("Primero");
    when(req.getPhoto()).thenReturn(null);
    when(req.getAllergens()).thenReturn(List.of("Gluten", "Huevos"));

    when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));
    when(dishRepository.findMaxDishIdByMenuId(10L)).thenReturn(Optional.of(1L));

    OfficialAllergen gluten = mock(OfficialAllergen.class);
    OfficialAllergen huevos = mock(OfficialAllergen.class);
    when(officialAllergenRepository.findById("Gluten")).thenReturn(Optional.of(gluten));
    when(officialAllergenRepository.findById("Huevos")).thenReturn(Optional.of(huevos));

    dishService.createDish(authentication, req);

    verify(dishRepository).save(any(Dish.class));
    verify(allergenDishRepository, times(2)).save(any(AllergenDish.class));
  }

  @Test
  void createDish_allergenNotFound_throwsIllegalArgumentException() {
    DishCReqDto req = mock(DishCReqDto.class);
    when(req.getMenuId()).thenReturn(10L);
    when(req.getTitle()).thenReturn("Pasta Carbonara");
    when(req.getDescription()).thenReturn("Descripcion");
    when(req.getCategory()).thenReturn("Primero");
    when(req.getPhoto()).thenReturn(null);
    when(req.getAllergens()).thenReturn(List.of("AlergenoInexistente"));

    when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));
    when(dishRepository.findMaxDishIdByMenuId(10L)).thenReturn(Optional.of(1L));
    when(officialAllergenRepository.findById("AlergenoInexistente")).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
      () -> dishService.createDish(authentication, req));
    verify(allergenDishRepository, never()).save(any());
  }


  @Test
  void createDish_menuNotBelongToChef_throwsIllegalArgumentException() {
    DishCReqDto req = mock(DishCReqDto.class);
    when(req.getMenuId()).thenReturn(10L);

    Chef otherChef = mock(Chef.class);
    when(otherChef.getId()).thenReturn(99L);
    Menu otherMenu = mock(Menu.class);
    when(otherMenu.getChef()).thenReturn(otherChef);

    when(menuRepository.findById(10L)).thenReturn(Optional.of(otherMenu));

    assertThrows(IllegalArgumentException.class,
      () -> dishService.createDish(authentication, req));
    verify(dishRepository, never()).save(any());
  }

  // ─── getDish ─────────────────────────────────────────────────────────────

  @Test
  void getDish_withName_returnsDishListFiltered() {
    Dish dish = mock(Dish.class);
    when(dish.getMenuId()).thenReturn(10L);
    when(dish.getDishId()).thenReturn(1L);
    when(dish.getTitle()).thenReturn("Pasta Carbonara");
    when(dish.getAllergenDishes()).thenReturn(new ArrayList<>());

    when(dishRepository.findByChefIdAndTitleContaining(1L, "Pasta")).thenReturn(List.of(dish));

    List<DishDto> result = dishService.getDish(authentication, "Pasta");

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(dishRepository).findByChefIdAndTitleContaining(1L, "Pasta");
    verify(dishRepository, never()).findAllByChefId(any());
  }

  @Test
  void getDish_chefNotFound_throwsRuntimeException() {
    when(chefRepository.findByUser_Username("mario@example.com")).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> dishService.getDish(authentication, null));
    verifyNoInteractions(dishRepository);
  }

  // ─── deleteDish ──────────────────────────────────────────────────────────

  @Test
  void deleteDish_success_deletesEntity() {
    Dish dish = mock(Dish.class);
    when(dish.getMenu()).thenReturn(menu);

    Dish.DishId dishId = new Dish.DishId(10L, 1L);
    when(dishRepository.findById(any(Dish.DishId.class))).thenReturn(Optional.of(dish));

    dishService.deleteDish(authentication, 10L, 1L);

    verify(dishRepository).delete(dish);
  }

  @Test
  void deleteDish_dishNotFound_throwsIllegalArgumentException() {
    when(dishRepository.findById(any(Dish.DishId.class))).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
      () -> dishService.deleteDish(authentication, 10L, 99L));
    verify(dishRepository, never()).delete(any());
  }

  // ─── updateDish ──────────────────────────────────────────────────────────

  @Test
  void updateDish_success_returnsUpdatedDto() {
    DishUReqDto req = mock(DishUReqDto.class);
    when(req.getMenuId()).thenReturn(10L);
    when(req.getDishId()).thenReturn(1L);
    when(req.getTitle()).thenReturn("Nuevo Título");
    when(req.getDescription()).thenReturn(null);
    when(req.getCategory()).thenReturn(null);
    when(req.getPhoto()).thenReturn(null);
    when(req.getAllergens()).thenReturn(null);

    Dish dish = mock(Dish.class);
    when(dish.getMenuId()).thenReturn(10L);
    when(dish.getDishId()).thenReturn(1L);
    when(dish.getTitle()).thenReturn("Nuevo Título");
    when(dish.getMenu()).thenReturn(menu);
    when(dish.getAllergenDishes()).thenReturn(new ArrayList<>());

    when(dishRepository.findById(any(Dish.DishId.class))).thenReturn(Optional.of(dish));
    when(dishRepository.save(dish)).thenReturn(dish);

    DishDto result = dishService.updateDish(authentication, req);

    assertNotNull(result);
    verify(dishRepository).save(dish);
  }

  @Test
  void updateDish_nullMenuId_throwsIllegalArgumentException() {
    DishUReqDto req = mock(DishUReqDto.class);
    when(req.getMenuId()).thenReturn(null);
    when(req.getDishId()).thenReturn(1L);

    assertThrows(IllegalArgumentException.class,
      () -> dishService.updateDish(authentication, req));
    verify(dishRepository, never()).findById(any());
  }
}
