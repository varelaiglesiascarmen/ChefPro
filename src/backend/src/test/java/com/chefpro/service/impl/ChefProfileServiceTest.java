package com.chefpro.service.impl;

import com.chefpro.backendjava.common.object.dto.ChefPublicDetailDto;
import com.chefpro.backendjava.common.object.dto.ChefUReqDto;
import com.chefpro.backendjava.common.object.dto.MenuPublicDetailDto;
import com.chefpro.backendjava.common.object.entity.*;
import com.chefpro.backendjava.repository.*;
import com.chefpro.backendjava.service.ChefProfileService;
import com.chefpro.backendjava.service.impl.ChefProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChefProfileServiceTest {

  private ChefProfileService chefProfileService;

  private ChefRepository chefRepository;
  private MenuRepository menuRepository;
  private ReviewRepository reviewRepository;
  private ReservaRepository reservaRepository;
  private Authentication authentication;

  private Chef chef;
  private UserLogin userLogin;

  @BeforeEach
  void setUp() {
    chefRepository  = mock(ChefRepository.class);
    menuRepository  = mock(MenuRepository.class);
    reviewRepository = mock(ReviewRepository.class);
    reservaRepository = mock(ReservaRepository.class);
    authentication  = mock(Authentication.class);

    chefProfileService = new ChefProfileServiceImpl(
      chefRepository, menuRepository, reviewRepository, reservaRepository
    );

    userLogin = mock(UserLogin.class);
    when(userLogin.getName()).thenReturn("Mario");
    when(userLogin.getLastname()).thenReturn("Rossi");
    when(userLogin.getEmail()).thenReturn("mario@example.com");
    when(userLogin.getPhoneNumber()).thenReturn("600000000");
    when(userLogin.getUsername()).thenReturn("mario@example.com");

    chef = mock(Chef.class);
    when(chef.getId()).thenReturn(1L);
    when(chef.getUser()).thenReturn(userLogin);
    when(chef.getPhoto()).thenReturn(null);
    when(chef.getBio()).thenReturn("Bio del chef");
    when(chef.getPrizes()).thenReturn("Premio");
    when(chef.getLocation()).thenReturn("Madrid");
    when(chef.getLanguages()).thenReturn("ES, EN");
    when(chef.getCoverPhoto()).thenReturn(null);
  }

  // ─── getChefPublicProfile ────────────────────────────────────────────────

  @Test
  void getChefPublicProfile_success_returnsDto() {
    when(chefRepository.findById(1L)).thenReturn(Optional.of(chef));
    when(menuRepository.findByChefIdWithDishes(1L)).thenReturn(new ArrayList<>());
    when(reviewRepository.findByReviewedUserIdWithReviewer(1L)).thenReturn(new ArrayList<>());
    when(reviewRepository.findAverageScoreByReviewedUserId(1L)).thenReturn(4.5);
    when(reviewRepository.countByReviewedUserId(1L)).thenReturn(10L);
    when(reservaRepository.findBusyDatesByChefId(1L)).thenReturn(new ArrayList<>());

    ChefPublicDetailDto result = chefProfileService.getChefPublicProfile(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Mario", result.getName());
    verify(chefRepository).findById(1L);
    verify(menuRepository).findByChefIdWithDishes(1L);
    verify(reviewRepository).findByReviewedUserIdWithReviewer(1L);
  }

  @Test
  void getChefPublicProfile_chefNotFound_throwsNoSuchElementException() {
    when(chefRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class,
      () -> chefProfileService.getChefPublicProfile(99L));
    verify(chefRepository).findById(99L);
    verifyNoInteractions(menuRepository, reviewRepository, reservaRepository);
  }

  // ─── getMenuPublicDetail ─────────────────────────────────────────────────

  @Test
  void getMenuPublicDetail_success_returnsDto() {
    Menu menu = mock(Menu.class);
    when(menu.getId()).thenReturn(5L);
    when(menu.getTitle()).thenReturn("Menú Test");
    when(menu.getDescription()).thenReturn("Descripción");
    when(menu.getPricePerPerson()).thenReturn(java.math.BigDecimal.valueOf(40));
    when(menu.getMinNumberDiners()).thenReturn(2);
    when(menu.getMaxNumberDiners()).thenReturn(6);
    when(menu.getKitchenRequirements()).thenReturn(null);
    when(menu.getDishes()).thenReturn(new ArrayList<>());
    when(menu.getChef()).thenReturn(chef);

    when(menuRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(menu));
    when(reservaRepository.findBusyDatesByChefId(1L)).thenReturn(new ArrayList<>());

    MenuPublicDetailDto result = chefProfileService.getMenuPublicDetail(5L);

    assertNotNull(result);
    assertEquals(5L, result.getId());
    assertEquals("Menú Test", result.getTitle());
    verify(menuRepository).findByIdWithDetails(5L);
    verify(reservaRepository).findBusyDatesByChefId(1L);
  }

  @Test
  void getMenuPublicDetail_menuNotFound_throwsNoSuchElementException() {
    when(menuRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class,
      () -> chefProfileService.getMenuPublicDetail(99L));
    verify(menuRepository).findByIdWithDetails(99L);
    verifyNoInteractions(reservaRepository);
  }

  // ─── updateChefProfile ───────────────────────────────────────────────────

  @Test
  void updateChefProfile_success_returnsUpdatedDto() {
    ChefUReqDto dto = mock(ChefUReqDto.class);
    when(dto.getBio()).thenReturn("Nueva bio");
    when(dto.getPhoto()).thenReturn(null);
    when(dto.getPrizes()).thenReturn(null);
    when(dto.getLocation()).thenReturn("Barcelona");
    when(dto.getLanguages()).thenReturn(null);
    when(dto.getCoverPhoto()).thenReturn(null);

    when(authentication.getName()).thenReturn("mario@example.com");
    when(chefRepository.findByUser_Username("mario@example.com")).thenReturn(Optional.of(chef));
    // findById es necesario porque updateChefProfile llama internamente a getChefPublicProfile(chef.getId())
    when(chefRepository.findById(1L)).thenReturn(Optional.of(chef));

    // Para el getChefPublicProfile interno que se llama al final
    when(menuRepository.findByChefIdWithDishes(1L)).thenReturn(new ArrayList<>());
    when(reviewRepository.findByReviewedUserIdWithReviewer(1L)).thenReturn(new ArrayList<>());
    when(reviewRepository.findAverageScoreByReviewedUserId(1L)).thenReturn(4.0);
    when(reviewRepository.countByReviewedUserId(1L)).thenReturn(5L);
    when(reservaRepository.findBusyDatesByChefId(1L)).thenReturn(new ArrayList<>());

    ChefPublicDetailDto result = chefProfileService.updateChefProfile(authentication, dto);

    assertNotNull(result);
    verify(chefRepository).save(chef);
    verify(chef).setBio("Nueva bio");
    verify(chef).setLocation("Barcelona");
  }

  @Test
  void updateChefProfile_chefNotFound_throwsNoSuchElementException() {
    ChefUReqDto dto = mock(ChefUReqDto.class);
    when(authentication.getName()).thenReturn("unknown@example.com");
    when(chefRepository.findByUser_Username("unknown@example.com")).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class,
      () -> chefProfileService.updateChefProfile(authentication, dto));
    verify(chefRepository, never()).save(any());
  }
}
