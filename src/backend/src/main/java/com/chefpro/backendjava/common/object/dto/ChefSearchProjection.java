package com.chefpro.backendjava.common.object.dto;

import java.math.BigDecimal;

/**
 * Proyección nativa para la query de búsqueda de chefs.
 * Los alias SQL deben coincidir exactamente con los nombres de estos métodos.
 */
public interface ChefSearchProjection {

  Long getUserId();
  String getUsername();
  String getName();
  String getLastname();
  String getPhoto();
  String getBio();
  String getLocation();
  Double getAvgScore();
  Long getReviewsCount();
  BigDecimal getStartingPrice();
}
