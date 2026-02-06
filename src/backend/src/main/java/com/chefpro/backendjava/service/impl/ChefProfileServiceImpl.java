package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.common.object.entity.*;
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

  // Mapeo estático de nombre de alérgeno (BD) → ID numérico (normativa UE)
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

  private final ChefRepository chefRepository;
  private final MenuRepository menuRepository;
  private final ReviewRepository reviewRepository;
  private final ReservaRepository reservaRepository;

  public ChefProfileServiceImpl(ChefRepository chefRepository,
                                MenuRepository menuRepository,
                                ReviewRepository reviewRepository,
                                ReservaRepository reservaRepository) {
    this.chefRepository = chefRepository;
    this.menuRepository = menuRepository;
    this.reviewRepository = reviewRepository;
    this.reservaRepository = reservaRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public ChefPublicDetailDto getChefPublicProfile(Long chefId) {
    Chef chef = chefRepository.findById(chefId)
      .orElseThrow(() -> new NoSuchElementException("Chef no encontrado con ID: " + chefId));

    UserLogin user = chef.getUser();

    // Menús con platos
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

    // Reseñas
    List<Review> reviews = reviewRepository.findByReviewedUserIdWithReviewer(chefId);
    List<ReviewSummaryDto> reviewSummaries = reviews.stream()
      .map(r -> ReviewSummaryDto.builder()
        .reviewerName(r.getReviewerUser().getName() + " " +
          r.getReviewerUser().getLastname().charAt(0) + ".")
        .date(r.getDate() != null ? r.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "")
        .score(r.getScore())
        .comment(r.getComment())
        .build())
      .collect(Collectors.toList());

    // Rating y conteo
    Double rating = reviewRepository.findAverageScoreByReviewedUserId(chefId);
    Long reviewsCount = reviewRepository.countByReviewedUserId(chefId);

    // Fechas ocupadas
    List<LocalDate> busyLocalDates = reservaRepository.findBusyDatesByChefId(chefId);
    List<String> busyDates = busyLocalDates.stream()
      .map(LocalDate::toString)
      .collect(Collectors.toList());

    String fullName = user.getName() + " " + user.getLastname();

    return ChefPublicDetailDto.builder()
      .id(chef.getId())
      .name(user.getName())
      .lastname(user.getLastname())
      .fullName(fullName)
      .email(user.getEmail())
      .phoneNumber(user.getPhoneNumber())
      .photo(chef.getPhoto())
      .bio(chef.getBio())
      .prizes(chef.getPrizes())
      .location(chef.getLocation())
      .languages(chef.getLanguages())
      .coverPhoto(chef.getCoverPhoto())
      .rating(Math.round(rating * 10.0) / 10.0)
      .reviewsCount(reviewsCount)
      .menus(menuSummaries)
      .reviews(reviewSummaries)
      .busyDates(busyDates)
      .build();
  }

  @Override
  @Transactional(readOnly = true)
  public MenuPublicDetailDto getMenuPublicDetail(Long menuId) {
    Menu menu = menuRepository.findByIdWithDetails(menuId)
      .orElseThrow(() -> new NoSuchElementException("Menú no encontrado con ID: " + menuId));

    // Forzar carga lazy de alérgenos
    menu.getDishes().forEach(dish -> dish.getAllergenDishes().size());

    Chef chef = menu.getChef();
    UserLogin chefUser = chef.getUser();
    String chefName = chefUser.getName() + " " + chefUser.getLastname();

    // Convertir platos con alérgenos numéricos
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

    // Fechas ocupadas del chef dueño del menú
    List<LocalDate> busyLocalDates = reservaRepository.findBusyDatesByChefId(chef.getId());
    List<String> busyDates = busyLocalDates.stream()
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
      .chefName(chefName)
      .chefPhoto(chef.getPhoto())
      .dishes(dishes)
      .busyDates(busyDates)
      .build();
  }

  @Override
  @Transactional
  public ChefPublicDetailDto updateChefProfile(Authentication authentication, ChefUReqDto dto) {
    Chef chef = chefRepository.findByUser_Username(authentication.getName())
      .orElseThrow(() -> new NoSuchElementException("Chef not found for user: " + authentication.getName()));

    // Partial update: only non-null DTO fields are applied
    if (dto.getPhoto() != null)     chef.setPhoto(dto.getPhoto());
    if (dto.getBio() != null)       chef.setBio(dto.getBio());
    if (dto.getPrizes() != null)    chef.setPrizes(dto.getPrizes());
    if (dto.getLocation() != null)  chef.setLocation(dto.getLocation());
    if (dto.getLanguages() != null) chef.setLanguages(dto.getLanguages());
    if (dto.getCoverPhoto() != null) chef.setCoverPhoto(dto.getCoverPhoto());

    chefRepository.save(chef);

    // Reuse the existing method to return the full updated profile
    return getChefPublicProfile(chef.getId());
  }
}
