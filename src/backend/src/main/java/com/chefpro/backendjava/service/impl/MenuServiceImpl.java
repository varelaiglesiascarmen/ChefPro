package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.MenuCReqDto;
import com.chefpro.backendjava.common.object.dto.MenuDTO;
import com.chefpro.backendjava.common.object.dto.MenuUReqDto;
import com.chefpro.backendjava.common.object.dto.PlatoDto;
import com.chefpro.backendjava.common.object.entity.Menu;
import com.chefpro.backendjava.common.object.entity.Plato;
import com.chefpro.backendjava.common.object.entity.UserLogin;
import com.chefpro.backendjava.repository.CustomUserRepository;
import com.chefpro.backendjava.repository.MenuRepository;
import com.chefpro.backendjava.repository.PlatoRepository;
import com.chefpro.backendjava.service.MenuService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component("MenuService")
public class MenuServiceImpl implements MenuService {

  private final MenuRepository menuRepository;
  private final PlatoRepository platoRepository;
  private final CustomUserRepository userRepository;

  public MenuServiceImpl(MenuRepository menuRepository,
                         PlatoRepository platoRepository,
                         CustomUserRepository userRepository) {
    this.menuRepository = menuRepository;
    this.platoRepository = platoRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public void createMenu(MenuCReqDto dto, Authentication authentication) {

    UserLogin chef = userRepository.findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

    if (dto.getTitle() == null || dto.getTitle().isBlank()) {
      throw new IllegalArgumentException("title is required");
    }

    if (dto.getPricePerPerson() == null || dto.getPricePerPerson().signum() <= 0) {
      throw new IllegalArgumentException("pricePerPerson must be > 0");
    }

    List<Long> dishIds = (dto.getDishesId() == null) ? List.of() : dto.getDishesId().stream()
      .filter(id -> id != null)
      .toList();

    List<Plato> foundDishes = List.of();

    if (!dishIds.isEmpty()) {
      foundDishes = platoRepository.findByChefUsernameAndIdIn(
        chef.getId(),
        dishIds
      );

      if (foundDishes.size() != dishIds.size()) {
        throw new IllegalArgumentException(
          "Some dishIds do not exist or do not belong to you"
        );
      }
    }

    Set<String> allergens = parseAllergens(dto.getAllergens());

    Menu menu = Menu.builder()
      .title(dto.getTitle())
      .description(dto.getDescription())
      .dishes(foundDishes)
      .allergens(allergens)
      .pricePerPerson(dto.getPricePerPerson())
      .deliveryAvailable(dto.isDeliveryAvailable())
      .cookAtClientHome(dto.isCookAtClientHome())
      .pickupAvailable(dto.isPickupAvailable())
      .chefId(chef.getId())
      .build();

    menuRepository.save(menu);
  }

  private Set<String> parseAllergens(String allergens) {
    if (allergens == null || allergens.isBlank()) return Set.of();

    return Arrays.stream(allergens.split(","))
      .map(String::trim)
      .filter(s -> !s.isBlank())
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public List<MenuDTO> listByChef(Authentication authentication) {

    List<Menu> menus = menuRepository.findByChefUsername(authentication.getName());

    return menus.stream()
      .map(menu -> MenuDTO.builder()
        .id(menu.getId())
        .title(menu.getTitle())
        .description(menu.getDescription())
        .dishes(mapPlatosToDtos(menu.getDishes()))
        .allergens(menu.getAllergens())
        .pricePerPerson(menu.getPricePerPerson())
        .deliveryAvailable(menu.isDeliveryAvailable())
        .cookAtClientHome(menu.isCookAtClientHome())
        .pickupAvailable(menu.isPickupAvailable())
        .chefUsername(authentication.getName())
        .createdAt(menu.getCreatedAt())
        .build()
      )
      .toList();
  }

    @Override
    @Transactional
    public void deleteMenu(Authentication authentication, Long idMenu) {

      UserLogin chef = userRepository.findByUsername(authentication.getName())
        .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

      Menu menu = menuRepository.findById(idMenu)
        .orElseThrow(() -> new RuntimeException("Menu not found: " + idMenu));

      if (!chef.getId().equals(menu.getChefId())) {
        throw new RuntimeException("Not allowed to delete this menu");
      }

      menuRepository.delete(menu);
    }

  @Override
  @Transactional
  public MenuDTO updateMenu(Authentication authentication, MenuUReqDto uReq) {

    UserLogin chef = userRepository.findByUsername(authentication.getName())
      .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

    if (uReq.getId() == null) {
      throw new IllegalArgumentException("id is required");
    }

    Menu menu = menuRepository.findById(uReq.getId())
      .orElseThrow(() -> new RuntimeException("Menu not found: " + uReq.getId()));

    if (!chef.getId().equals(menu.getChefId())) {
      throw new RuntimeException("Not allowed to update this menu");
    }

    // --- aplicar cambios (mínimos) ---
    if (uReq.getTitle() != null && !uReq.getTitle().isBlank()) {
      menu.setTitle(uReq.getTitle());
    }

    if (uReq.getDescription() != null) {
      menu.setDescription(uReq.getDescription());
    }

    if (uReq.getPricePerPerson() != null && uReq.getPricePerPerson().signum() > 0) {
      menu.setPricePerPerson(uReq.getPricePerPerson());
    }

    // boolean siempre viene con valor, así que si los quieres opcionales,
    // necesitas Boolean en el dto. Aquí los aplico tal cual:
    menu.setDeliveryAvailable(uReq.isDeliveryAvailable());
    menu.setCookAtClientHome(uReq.isCookAtClientHome());
    menu.setPickupAvailable(uReq.isPickupAvailable());

    // Allergens (String CSV -> Set<String>)
    if (uReq.getAllergens() != null) {
      menu.setAllergens(parseAllergens(uReq.getAllergens()));
    }

    // Dishes: si vienen en el request, actualizamos la relación
    if (uReq.getDishes() != null) {
      List<Long> dishIds = uReq.getDishes(); // ← CAMBIO: ya son Long en el DTO

      List<Plato> dishes = dishIds.isEmpty()
        ? List.of()
        : platoRepository.findAllById(dishIds);

      if (!dishIds.isEmpty() && dishes.size() != dishIds.size()) {
        throw new IllegalArgumentException("Some dishIds do not exist");
      }

      menu.setDishes(dishes);
    }

    Menu saved = menuRepository.save(menu);

    return MenuDTO.builder()
      .id(saved.getId())
      .title(saved.getTitle())
      .description(saved.getDescription())
      .dishes(mapPlatosToDtos(saved.getDishes()))
      .allergens(saved.getAllergens())
      .pricePerPerson(saved.getPricePerPerson())
      .deliveryAvailable(saved.isDeliveryAvailable())
      .cookAtClientHome(saved.isCookAtClientHome())
      .pickupAvailable(saved.isPickupAvailable())
      .chefUsername(authentication.getName())
      .createdAt(saved.getCreatedAt())
      .build();
  }

  private List<PlatoDto> mapPlatosToDtos(List<Plato> platos) {
    if (platos == null) return List.of();

    return platos.stream()
      .map(plato -> new PlatoDto(
        plato.getId(),
        plato.getTitle(),
        plato.getDescription(),
        null,
        null,
        null,
        null,
        null
      ))
      .toList();
  }
}
