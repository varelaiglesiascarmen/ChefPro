package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.ChefSearchDto;
import com.chefpro.backendjava.repository.ChefSearchRepository;
import com.chefpro.backendjava.service.ChefSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ChefSearchServiceImpl implements ChefSearchService {

  private final ChefSearchRepository repo;

  public ChefSearchServiceImpl(ChefSearchRepository repo) {
    this.repo = repo;
  }

  @Override
  public Page<ChefSearchDto> search(
    String q,
    String date,
    Integer minPrice,
    Integer maxPrice,
    Integer guests,
    String diets,
    boolean top,
    Pageable pageable
  ) {
    LocalDate parsedDate = (date == null || date.isBlank()) ? null : LocalDate.parse(date);
    BigDecimal min = (minPrice == null) ? null : BigDecimal.valueOf(minPrice);
    BigDecimal max = (maxPrice == null) ? null : BigDecimal.valueOf(maxPrice);

    return repo.searchChefs(q, parsedDate, min, max, guests, pageable);
  }
}
