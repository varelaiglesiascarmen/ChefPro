package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.ChefSearchDto;
import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.repository.ChefRepository;
import com.chefpro.backendjava.service.ChefSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("chefSearchService")
public class ChefSearchServiceImpl implements ChefSearchService {

  private final ChefRepository chefRepository;

  public ChefSearchServiceImpl(ChefRepository chefRepository) {
    this.chefRepository = chefRepository;
  }

  /**
   * Busca chefs con filtros opcionales
   *
   * @param name Nombre del chef (opcional)
   * @param date Fecha para verificar disponibilidad (opcional)
   * @param pageable Parámetros de paginación
   * @return Página de DTOs de chefs
   */
  @Override
  public Page<ChefSearchDto> search(String name, LocalDate date, Pageable pageable) {
    Page<Chef> chefs;

    // Aplicar filtros según los parámetros recibidos
    if (name != null && !name.isBlank() && date != null) {
      // Filtrar por nombre Y disponibilidad
      chefs = chefRepository.findByNameAndAvailableOnDate(name.trim(), date, pageable);
    } else if (name != null && !name.isBlank()) {
      // Solo filtrar por nombre
      chefs = chefRepository.findByNameContaining(name.trim(), pageable);
    } else if (date != null) {
      // Solo filtrar por disponibilidad
      chefs = chefRepository.findAvailableOnDate(date, pageable);
    } else {
      // Sin filtros, devolver todos
      chefs = chefRepository.findAll(pageable);
    }

    // Convertir entidades a DTOs
    return chefs.map(this::toDto);
  }

  /**
   * Convierte una entidad Chef a ChefSearchDto
   */
  private ChefSearchDto toDto(Chef chef) {
    return ChefSearchDto.builder()
      .id(chef.getId())
      .name(chef.getUser().getName())
      .lastname(chef.getUser().getLastname())
      .username(chef.getUser().getUsername())
      .email(chef.getUser().getEmail())
      .phoneNumber(chef.getUser().getPhoneNumber())
      .photo(chef.getPhoto())
      .bio(chef.getBio())
      .prizes(chef.getPrizes())
      .build();
  }
}
