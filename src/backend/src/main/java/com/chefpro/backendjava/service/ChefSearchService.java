package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.ChefSearchResultDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ChefSearchService {

  ChefSearchResultDto search(
    String q,
    LocalDate date,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Integer guests,
    List<String> allergens
  );
}
