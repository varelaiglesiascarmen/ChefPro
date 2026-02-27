package com.chefpro.backendjava.common.object.dto;

import java.math.BigDecimal;

/**
 * Proyección nativa para la query de búsqueda de menús.
 * Los alias SQL deben coincidir exactamente con los nombres de estos métodos.
 */
public interface MenuSearchProjection {

  Long getMenuId();
  String getMenuTitle();
  String getMenuDescription();
  BigDecimal getPricePerPerson();
  Integer getMinDiners();
  Integer getMaxDiners();
  Long getChefId();
  String getChefName();
  String getChefLastname();
  String getChefPhoto();
  String getChefLocation();
  Double getAvgScore();
  Long getReviewsCount();
}
