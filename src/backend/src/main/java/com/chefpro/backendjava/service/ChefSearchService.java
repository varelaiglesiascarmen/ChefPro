package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.ChefSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ChefSearchService {
  Page<ChefSearchDto> search(String q, LocalDate date, Pageable pageable);
}
