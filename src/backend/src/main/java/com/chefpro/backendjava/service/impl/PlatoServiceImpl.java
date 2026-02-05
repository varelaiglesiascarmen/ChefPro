package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.DishDto;
import com.chefpro.backendjava.common.object.dto.DishCReqDto;
import com.chefpro.backendjava.common.object.dto.DishUReqDto;
import com.chefpro.backendjava.common.object.entity.*;
import com.chefpro.backendjava.repository.*;
import com.chefpro.backendjava.service.PlatoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("platoService")
public class PlatoServiceImpl implements PlatoService {

  private final ChefRepository chefRepository;
  private final MenuRepository menuRepository;
  private final DishRepository dishRepository;
  private final AllergenDishRepository allergenDishRepository;
  private final OfficialAllergenRepository officialAllergenRepository;

  public PlatoServiceImpl(ChefRepository chefRepository, MenuRepository menuRepository, DishRepository dishRepository, AllergenDishRepository allergenDishRepository, OfficialAllergenRepository officialAllergenRepository) {
    this.chefRepository = chefRepository;
    this.menuRepository = menuRepository;
    this.dishRepository = dishRepository;
    this.allergenDishRepository = allergenDishRepository;
    this.officialAllergenRepository = officialAllergenRepository;
  }


  @Override
  @Transactional
  public void createDish(Authentication auth, DishCReqDto cReq) {

    // 1. Obtener el chef autenticado
    Chef chef = chefRepository.findByUser_Username(auth.getName())
      .orElseThrow(() -> new RuntimeException("Authenticated chef not found"));

    // 2. Validar que el menú existe y pertenece al chef
    Menu menu = menuRepository.findById(cReq.getMenuId())
      .orElseThrow(() -> new IllegalArgumentException("Menu not found"));

    if (!menu.getChef().getId().equals(chef.getId())) {
      throw new IllegalArgumentException("This menu does not belong to the authenticated chef");
    }

    // 3. Validaciones
    if (cReq.getTitle() == null || cReq.getTitle().isBlank()) {
      throw new IllegalArgumentException("Title is required");
    }

    // 4. Calcular el siguiente dish_ID para este menú
    Long nextDishId = dishRepository.findMaxDishIdByMenuId(cReq.getMenuId())
      .map(maxId -> maxId + 1)
      .orElse(1L);

    // 5. Crear el plato
    Dish plato = Dish.builder()
      .menuId(cReq.getMenuId())
      .dishId(nextDishId)
      .title(cReq.getTitle())
      .description(cReq.getDescription())
      .category(cReq.getCategory())
      .menu(menu)
      .allergenDishes(new ArrayList<>())
      .build();

    dishRepository.save(plato);

    // 6. Asociar alérgenos si existen
    if (cReq.getAllergens() != null && !cReq.getAllergens().isEmpty()) {
      for (String allergenName : cReq.getAllergens()) {
        // Verificar que el alérgeno existe en la lista oficial
        OfficialAllergen officialAllergen = officialAllergenRepository
          .findById(allergenName)
          .orElseThrow(() -> new IllegalArgumentException("Allergen not found: " + allergenName));

        AllergenDish allergenDish = AllergenDish.builder()
          .menuId(plato.getMenuId())
          .dishId(plato.getDishId())
          .allergen(allergenName)
          .dish(plato)
          .officialAllergen(officialAllergen)
          .build();

        allergenDishRepository.save(allergenDish);
      }
    }
  }

  @Override
  public List<DishDto> getDish(Authentication authentication, String nombrePlato) {

    // 1. Obtener el chef autenticado
    Chef chef = chefRepository.findByUser_Username(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Authenticated chef not found"));

    List<Dish> dishes;

    // 2. Si se proporciona nombre de plato, buscar por título
    if (nombrePlato != null && !nombrePlato.isEmpty()) {
      dishes = dishRepository.findByChefIdAndTitleContaining(chef.getId(), nombrePlato);
    } else {
      // 3. Si no hay filtro, devolver todos los platos del chef
      dishes = dishRepository.findAllByChefId(chef.getId());
    }

    // 4. Convertir a DTO
    return dishes.stream()
      .map(dish -> convertToDto(dish, chef))
      .collect(Collectors.toList());
  }

  // Método auxiliar para convertir entidad a DTO
  private DishDto convertToDto(Dish dish, Chef chef) {
    // Obtener los nombres de los alérgenos
    List<String> allergens = dish.getAllergenDishes().stream()
      .map(AllergenDish::getAllergen)
      .collect(Collectors.toList());

    // Obtener el nombre completo del chef
    String creatorName = chef.getUser().getName() != null && chef.getUser().getLastname() != null
      ? chef.getUser().getName() + " " + chef.getUser().getLastname()
      : chef.getUser().getUsername();

    return DishDto.builder()
      .menuId(dish.getMenuId())
      .dishId(dish.getDishId())
      .title(dish.getTitle())
      .description(dish.getDescription())
      .category(dish.getCategory())
      .creator(creatorName)
      .allergens(allergens)
      .build();
  }

  @Override
  @Transactional
  public void deleteDish(Authentication authentication, Long menuId, Long dishId) {

    // 1. Obtener el chef autenticado
    Chef chef = chefRepository.findByUser_Username(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Authenticated chef not found"));

    // 2. Buscar el plato con su clave compuesta
    Dish.DishId dishIdObj = new Dish.DishId(menuId, dishId);
    Dish dish = dishRepository.findById(dishIdObj)
      .orElseThrow(() -> new IllegalArgumentException("Dish not found"));

    // 3. Verificar que el plato pertenece a un menú del chef autenticado
    if (!dish.getMenu().getChef().getId().equals(chef.getId())) {
      throw new IllegalArgumentException("This dish does not belong to the authenticated chef");
    }

    // 4. Eliminar el plato (los alérgenos se borran automáticamente por cascade)
    dishRepository.delete(dish);
  }

  @Override
  @Transactional
  public DishDto updateDish(Authentication authentication, DishUReqDto uReq) {

    // 1. Obtener el chef autenticado
    Chef chef = chefRepository.findByUser_Username(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Authenticated chef not found"));

    // 2. Validaciones
    if (uReq.getMenuId() == null || uReq.getDishId() == null) {
      throw new IllegalArgumentException("menuId and dishId are required");
    }

    // 3. Buscar el plato con su clave compuesta
    Dish.DishId dishIdObj = new Dish.DishId(uReq.getMenuId(), uReq.getDishId());
    Dish dish = dishRepository.findById(dishIdObj)
      .orElseThrow(() -> new IllegalArgumentException("Dish not found"));

    // 4. Verificar que el plato pertenece a un menú del chef autenticado
    if (!dish.getMenu().getChef().getId().equals(chef.getId())) {
      throw new IllegalArgumentException("This dish does not belong to the authenticated chef");
    }

    // 5. Actualizar los campos del plato
    if (uReq.getTitle() != null && !uReq.getTitle().isBlank()) {
      dish.setTitle(uReq.getTitle());
    }

    if (uReq.getDescription() != null) {
      dish.setDescription(uReq.getDescription());
    }

    if (uReq.getCategory() != null) {
      dish.setCategory(uReq.getCategory());
    }

    // 6. Actualizar alérgenos si se proporcionan
    if (uReq.getAllergens() != null) {
      // Eliminar alérgenos existentes
      allergenDishRepository.deleteAll(dish.getAllergenDishes());
      dish.getAllergenDishes().clear();

      // Añadir nuevos alérgenos
      for (String allergenName : uReq.getAllergens()) {
        OfficialAllergen officialAllergen = officialAllergenRepository
          .findById(allergenName)
          .orElseThrow(() -> new IllegalArgumentException("Allergen not found: " + allergenName));

        AllergenDish allergenDish = AllergenDish.builder()
          .menuId(dish.getMenuId())
          .dishId(dish.getDishId())
          .allergen(allergenName)
          .dish(dish)
          .officialAllergen(officialAllergen)
          .build();

        allergenDishRepository.save(allergenDish);
        dish.getAllergenDishes().add(allergenDish);
      }
    }

    // 7. Guardar cambios
    dishRepository.save(dish);

    // 8. Convertir a DTO y devolver
    return convertToDto(dish, chef);
  }
}
