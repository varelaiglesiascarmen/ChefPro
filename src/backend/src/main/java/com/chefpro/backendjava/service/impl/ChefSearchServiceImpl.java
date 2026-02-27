package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.repository.ChefSearchRepository;
import com.chefpro.backendjava.repository.MenuSearchRepository;
import com.chefpro.backendjava.service.ChefSearchService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("chefSearchService")
public class ChefSearchServiceImpl implements ChefSearchService {

  private static final int SUGGESTION_LIMIT = 6;

  private final ChefSearchRepository chefSearchRepository;
  private final MenuSearchRepository menuSearchRepository;

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

    // 1. Cities matching the search text
    List<String> cities = chefSearchRepository.findMatchingCities(query);

    // 2. Chefs: multi-word support — each word must match name or lastname
    List<ChefSearchDto> chefs = searchChefsMultiWord(query, date);

    // 3. Menus (food types) matching the search text, applying filters only when filled
    List<MenuSearchDto> menus = menuSearchRepository
      .searchMenus(query, date, minPrice, maxPrice, guests, allergenFilter)
      .stream()
      .map(this::toMenuDto)
      .toList();

    // If no results and there was a search query, return random suggestions
    if (cities.isEmpty() && chefs.isEmpty() && menus.isEmpty() && query != null) {
      List<MenuSearchDto> randomMenus = menuSearchRepository
        .findRandomMenuSuggestions(SUGGESTION_LIMIT)
        .stream()
        .map(this::toMenuDto)
        .toList();

      return ChefSearchResultDto.builder()
        .cities(List.of())
        .chefs(List.of())
        .menus(randomMenus)
        .noResults(true)
        .build();
    }

    return ChefSearchResultDto.builder()
      .cities(cities)
      .chefs(chefs)
      .menus(menus)
      .noResults(false)
      .build();
  }

  /**
   * Multi-word chef search: queries DB with the full text, then filters
   * so that every individual word matches somewhere in "name lastname".
   */
  private List<ChefSearchDto> searchChefsMultiWord(String query, LocalDate date) {
    if (query == null) {
      return chefSearchRepository.searchChefs(null, date)
        .stream()
        .map(this::toChefDto)
        .toList();
    }

    String[] words = query.split("\\s+");

    // Query DB with the full phrase first (catches exact matches)
    List<ChefSearchDto> candidates = new ArrayList<>(chefSearchRepository
      .searchChefs(query, date)
      .stream()
      .map(this::toChefDto)
      .toList());

    // For single-word queries, no extra filtering needed
    if (words.length <= 1) {
      return candidates;
    }

    // Multi-word: also query with each individual word and merge results
    for (String word : words) {
      List<ChefSearchDto> partial = chefSearchRepository
        .searchChefs(word.trim(), date)
        .stream()
        .map(this::toChefDto)
        .toList();
      for (ChefSearchDto chef : partial) {
        if (candidates.stream().noneMatch(c -> c.getId().equals(chef.getId()))) {
          candidates.add(chef);
        }
      }
    }

    // Filter: every word must appear in "name lastname"
    return candidates.stream()
      .filter(chef -> {
        String fullName = ((chef.getName() != null ? chef.getName() : "") + " "
          + (chef.getLastname() != null ? chef.getLastname() : "")).toLowerCase();
        return Arrays.stream(words)
          .allMatch(w -> fullName.contains(w.toLowerCase()));
      })
      .toList();
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
