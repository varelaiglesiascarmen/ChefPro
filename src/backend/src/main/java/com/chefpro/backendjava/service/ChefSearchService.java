package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.ChefSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChefSearchService {
  Page<ChefSearchDto> search(
    String q,
    String date,
    Integer minPrice,
    Integer maxPrice,
    Integer guests,
    String dietsCsv,
    boolean onlyTopRated,
    Pageable pageable
  );
}
