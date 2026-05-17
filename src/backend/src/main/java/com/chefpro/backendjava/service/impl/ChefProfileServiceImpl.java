package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.common.object.entity.*;
import com.chefpro.backendjava.common.util.ChefResolver;
import com.chefpro.backendjava.repository.*;
import com.chefpro.backendjava.service.ChefProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChefProfileServiceImpl implements ChefProfileService {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  // Allergen name (DB) → EU regulation numeric ID
  private static final Map<String, Integer> ALLERGEN_NAME_TO_ID = Map.ofEntries(
    Map.entry("Gluten", 1),
    Map.entry("Crustáceos", 2),
    Map.entry("Huevos", 3),
    Map.entry("Pescado", 4),
    Map.entry("Cacahuetes", 5),
    Map.entry("Soja", 6),
    Map.entry("Lácteos", 7),
    Map.entry("Frutos de cáscara", 8),
    Map.entry("Apio", 9),
    Map.entry("Mostaza", 10),
    Map.entry("Granos de sésamo", 11),
    Map.entry("Dióxido de azufre y sulfitos", 12),
    Map.entry("Altramuces", 13),
    Map.entry("Moluscos", 14)
  );

  private final ChefResolver chefResolver;
  private final ChefRepository chefRepository;
  private final MenuRepository menuRepository;
  private final ReviewRepository reviewRepository;
  private final ReservaRepository reservaRepository;

  public ChefProfileServiceImpl(ChefResolver chefResolver,
                                ChefRepository chefRepository,
                                MenuRepository menuRepository,
                                ReviewRepository reviewRepository,
                                ReservaRepository reservaRepository) {
    this.chefResolver = chefResolver;
    this.chefRepository = chefRepository;
    this.menuRepository = menuRepository;
    this.reviewRepository = reviewRepository;
    this.reservaRepository = reservaRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public ChefPublicDetailDto getChefPublicProfile(Long chefId) {
    Chef chef = chefRepository.findById(chefId)
      .orElseThrow(() -> new NoSuchElementException("Chef not found: " + chefId));

    UserLogin user = chef.getUser();

    List<Menu> menus = menuRepository.findByChefIdWithDishes(chefId);
    List<MenuSummaryDto> menuSummaries = menus.stream()
      .map(m -> MenuSummaryDto.builder()
        .id(m.getId())
        .title(m.getTitle())
        .description(m.getDescription())
        .price(m.getPricePerPerson())
        .dishesCount(m.getDishes().size())
        .minDiners(m.getMinNumberDiners())
        .maxDiners(m.getMaxNumberDiners())
        .build())
      .collect(Collectors.toList());

    // Obtener reviews donde el usuario es el chef o el comensal puntuado
    List<Review> reviews = reviewRepository.findByReviewedUserIdWithReviewer(chefId);
    List<ReviewSummaryDto> reviewSummaries = reviews.stream()
      .map(r -> ReviewSummaryDto.builder()
        .reviewerName(r.getReviewerUser().getName() + " " + r.getReviewerUser().getLastname().charAt(0) + ".")
        .date(r.getDate() != null ? r.getDate().format(DATE_FORMATTER) : "")
        .score(r.getScore())
        .comment(r.getComment())
        .build())
      .collect(Collectors.toList());

    Double rating = reviewRepository.findAverageScoreByReviewedUserId(chefId);
    Long reviewsCount = reviewRepository.countByReviewedUserId(chefId);

    List<String> busyDates = reservaRepository.findBusyDatesByChefId(chefId).stream()
      .map(LocalDate::toString)
      .collect(Collectors.toList());

    return ChefPublicDetailDto.builder()
      .id(chef.getId())
      .name(user.getName())
      .lastname(user.getLastname())
      .fullName(user.getName() + " " + user.getLastname())
      .email(user.getEmail())
      .phoneNumber(user.getPhoneNumber())
      .photo(chef.getPhoto())
      .bio(chef.getBio())
      .prizes(chef.getPrizes())
      .location(chef.getLocation())
      .languages(chef.getLanguages())
      .coverPhoto(chef.getCoverPhoto())
      .rating(rating != null ? Math.round(rating * 10.0) / 10.0 : 0.0)
      .reviewsCount(reviewsCount != null ? reviewsCount : 0L)
      .menus(menuSummaries)
      .reviews(reviewSummaries)
      .busyDates(busyDates)
      .build();
  }

  @Override
  @Transactional(readOnly = true)
  public MenuPublicDetailDto getMenuPublicDetail(Long menuId) {
    Menu menu = menuRepository.findByIdWithDetails(menuId)
      .orElseThrow(() -> new NoSuchElementException("Menu not found: " + menuId));

    menu.getDishes().forEach(dish -> dish.getAllergenDishes().size());

    Chef chef = menu.getChef();
    UserLogin chefUser = chef.getUser();

    List<DishPublicDto> dishes = menu.getDishes().stream()
      .map(dish -> {
        List<Integer> allergenIds = dish.getAllergenDishes().stream()
          .map(ad -> ALLERGEN_NAME_TO_ID.getOrDefault(ad.getAllergen(), 0))
          .filter(id -> id > 0)
          .sorted()
          .collect(Collectors.toList());

        return DishPublicDto.builder()
          .dishId(dish.getDishId())
          .title(dish.getTitle())
          .description(dish.getDescription())
          .category(dish.getCategory())
          .allergenIds(allergenIds)
          .build();
      })
      .collect(Collectors.toList());

    List<String> busyDates = reservaRepository.findBusyDatesByChefId(chef.getId()).stream()
      .map(LocalDate::toString)
      .collect(Collectors.toList());

    return MenuPublicDetailDto.builder()
      .id(menu.getId())
      .title(menu.getTitle())
      .description(menu.getDescription())
      .price(menu.getPricePerPerson())
      .minDiners(menu.getMinNumberDiners())
      .maxDiners(menu.getMaxNumberDiners())
      .requirements(menu.getKitchenRequirements())
      .chefId(chef.getId())
      .chefName(chefUser.getName() + " " + chefUser.getLastname())
      .chefPhoto(chef.getPhoto())
      .dishes(dishes)
      .busyDates(busyDates)
      .build();
  }

  @Override
  @Transactional
  public ChefPublicDetailDto updateChefProfile(Authentication authentication, ChefUReqDto dto) {
    Chef chef = chefResolver.resolve(authentication);

    if (dto.getPhoto() != null)       chef.setPhoto(dto.getPhoto());
    if (dto.getBio() != null)         chef.setBio(dto.getBio());
    if (dto.getPrizes() != null)      chef.setPrizes(dto.getPrizes());
    if (dto.getLocation() != null)    chef.setLocation(dto.getLocation());
    if (dto.getLanguages() != null)   chef.setLanguages(dto.getLanguages());
    if (dto.getCoverPhoto() != null)  chef.setCoverPhoto(dto.getCoverPhoto());

    chefRepository.save(chef);
    return getChefPublicProfile(chef.getId());
  }
}
