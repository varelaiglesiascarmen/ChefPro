package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.repository.ChefSearchRepository;
import com.chefpro.backendjava.repository.MenuSearchRepository;
import com.chefpro.backendjava.service.ChefSearchService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
<<<<<<< HEAD
import java.util.Collections;
=======
>>>>>>> 92e126861fcf8bdb5428abe2ca3b3b2043c4af64
import java.util.List;

@Component("chefSearchService")
public class ChefSearchServiceImpl implements ChefSearchService {

<<<<<<< HEAD
  private final ChefSearchRepository chefSearchRepository;
  private final MenuSearchRepository menuSearchRepository;

=======
  private static final int SUGGESTION_LIMIT = 6;

  private final ChefSearchRepository chefSearchRepository;
  private final MenuSearchRepository menuSearchRepository;

>>>>>>> 92e126861fcf8bdb5428abe2ca3b3b2043c4af64
  public ChefSearchServiceImpl(
    ChefSearchRepository chefSearchRepository,
    MenuSearchRepository menuSearchRepository
  ) {
    this.chefSearchRepository = chefSearchRepository;
    this.menuSearchRepository = menuSearchRepository;
  }

  @Override
  public ChefSearchResultDto search(
    String q,
    LocalDate date,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Integer guests,
    List<String> allergens
  ) {
    String query = (q != null && !q.isBlank()) ? q.trim() : null;
    List<String> allergenFilter = (allergens != null && !allergens.isEmpty()) ? allergens : null;

    List<ChefSearchDto> chefs = chefSearchRepository
      .searchChefs(query, date)
      .stream()
      .map(this::toChefDto)
      .toList();

    List<MenuSearchDto> menus = menuSearchRepository
      .searchMenus(query, date, minPrice, maxPrice, guests, allergenFilter)
      .stream()
      .map(this::toMenuDto)
      .toList();

    // Si no hay ningún resultado, devolver menús aleatorios como sugerencias
    // Solo se activa cuando había texto de búsqueda (no tiene sentido en carga inicial)
    if (chefs.isEmpty() && menus.isEmpty() && query != null) {
      List<MenuSearchDto> randomMenus = menuSearchRepository
<<<<<<< HEAD
        .searchMenus(null, null, null, null, null, null)
        .stream()
        .map(this::toMenuDto)
        .collect(java.util.stream.Collectors.toList());

      Collections.shuffle(randomMenus);
=======
        .findRandomMenuSuggestions(SUGGESTION_LIMIT)
        .stream()
        .map(this::toMenuDto)
        .toList();
>>>>>>> 92e126861fcf8bdb5428abe2ca3b3b2043c4af64

      return ChefSearchResultDto.builder()
        .chefs(List.of())
        .menus(randomMenus)
        .noResults(true)
        .build();
    }

    return ChefSearchResultDto.builder()
      .chefs(chefs)
      .menus(menus)
      .noResults(false)
      .build();
  }

  private ChefSearchDto toChefDto(ChefSearchProjection p) {
    return ChefSearchDto.builder()
      .id(p.getUserId())
      .username(p.getUsername())
      .name(p.getName())
      .lastname(p.getLastname())
      .photo(p.getPhoto())
      .bio(p.getBio())
      .location(p.getLocation())
      .avgScore(p.getAvgScore())
      .reviewsCount(p.getReviewsCount())
      .startingPrice(p.getStartingPrice())
      .build();
  }

  private MenuSearchDto toMenuDto(MenuSearchProjection p) {
    return MenuSearchDto.builder()
      .menuId(p.getMenuId())
      .menuTitle(p.getMenuTitle())
      .menuDescription(p.getMenuDescription())
      .pricePerPerson(p.getPricePerPerson())
      .minDiners(p.getMinDiners())
      .maxDiners(p.getMaxDiners())
      .chefId(p.getChefId())
      .chefName(p.getChefName())
      .chefLastname(p.getChefLastname())
      .chefPhoto(p.getChefPhoto())
      .chefLocation(p.getChefLocation())
      .avgScore(p.getAvgScore())
      .reviewsCount(p.getReviewsCount())
      .build();
  }
}
