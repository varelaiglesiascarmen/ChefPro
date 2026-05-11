package com.chefpro.backendjava.common.util;

import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.repository.ChefRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class ChefResolver {

  private final ChefRepository chefRepository;

  public ChefResolver(ChefRepository chefRepository) {
    this.chefRepository = chefRepository;
  }

  public Chef resolve(Authentication authentication) {
    return chefRepository.findByUser_Username(authentication.getName())
      .orElseThrow(() -> new NoSuchElementException("Chef not found for user: " + authentication.getName()));
  }
}
