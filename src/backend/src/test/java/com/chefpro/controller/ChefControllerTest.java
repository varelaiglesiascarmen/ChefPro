package com.chefpro.controller;

import com.chefpro.backendjava.common.object.dto.*;
import com.chefpro.backendjava.controller.ChefController;
import com.chefpro.backendjava.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChefControllerTest {

  private MenuService menuService;
  private DishService dishService;
  private ChefSearchService chefSearchService;
  private ChefProfileService chefProfileService;
  private PhotoUploadService photoUploadService;
  private Authentication authentication;

  private ChefController controller;

  @BeforeEach
  void setUp() {
    menuService        = mock(MenuService.class);
    dishService        = mock(DishService.class);
    chefSearchService  = mock(ChefSearchService.class);
    chefProfileService = mock(ChefProfileService.class);
    photoUploadService = mock(PhotoUploadService.class);
    authentication     = mock(Authentication.class);

    controller = new ChefController(
      menuService, dishService, chefSearchService, chefProfileService, photoUploadService
    );
  }

  // ─── GET /menus ──────────────────────────────────────────────────────────

  @Test
  void getMenusDelChef_success_returnsMenuList() {
    MenuDTO menu = mock(MenuDTO.class);
    when(menuService.listByChef(authentication)).thenReturn(List.of(menu));

    List<MenuDTO> result = controller.getMenusDelChef(authentication);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(menuService).listByChef(authentication);
  }

  @Test
  void getMenusDelChef_serviceThrows_propagatesException() {
    when(menuService.listByChef(authentication)).thenThrow(new RuntimeException("DB error"));

    assertThrows(RuntimeException.class, () -> controller.getMenusDelChef(authentication));
    verify(menuService).listByChef(authentication);
  }

  // ─── POST /menus ─────────────────────────────────────────────────────────

  @Test
  void crearMenu_success_returns201WithBody() {
    MenuCReqDto req = mock(MenuCReqDto.class);
    MenuDTO created = mock(MenuDTO.class);
    when(menuService.createMenu(req, authentication)).thenReturn(created);

    ResponseEntity<MenuDTO> response = controller.crearMenu(req, authentication);

    assertEquals(201, response.getStatusCode().value());
    assertEquals(created, response.getBody());
    verify(menuService).createMenu(req, authentication);
  }

  @Test
  void crearMenu_serviceThrows_propagatesException() {
    MenuCReqDto req = mock(MenuCReqDto.class);
    when(menuService.createMenu(req, authentication)).thenThrow(new IllegalArgumentException("Invalid data"));

    assertThrows(IllegalArgumentException.class, () -> controller.crearMenu(req, authentication));
    verify(menuService).createMenu(req, authentication);
  }

  // ─── DELETE /menus/{id} ──────────────────────────────────────────────────

  @Test
  void deleteMenu_success_returns204() {
    doNothing().when(menuService).deleteMenu(authentication, 1L);

    ResponseEntity<Void> response = controller.deleteMenu(authentication, 1L);

    assertEquals(204, response.getStatusCode().value());
    verify(menuService).deleteMenu(authentication, 1L);
  }

  @Test
  void deleteMenu_serviceThrows_propagatesException() {
    doThrow(new NoSuchElementException("Menu not found")).when(menuService).deleteMenu(authentication, 99L);

    assertThrows(NoSuchElementException.class, () -> controller.deleteMenu(authentication, 99L));
    verify(menuService).deleteMenu(authentication, 99L);
  }

  // ─── PATCH /menus ────────────────────────────────────────────────────────

  @Test
  void patchMenu_success_returnsUpdatedMenu() {
    MenuUReqDto req = mock(MenuUReqDto.class);
    MenuDTO updated = mock(MenuDTO.class);
    when(menuService.updateMenu(authentication, req)).thenReturn(updated);

    MenuDTO result = controller.patchMenu(authentication, req);

    assertEquals(updated, result);
    verify(menuService).updateMenu(authentication, req);
  }

  @Test
  void patchMenu_serviceThrows_propagatesException() {
    MenuUReqDto req = mock(MenuUReqDto.class);
    when(menuService.updateMenu(authentication, req)).thenThrow(new RuntimeException("Update failed"));

    assertThrows(RuntimeException.class, () -> controller.patchMenu(authentication, req));
    verify(menuService).updateMenu(authentication, req);
  }

  // ─── GET /menus/public ───────────────────────────────────────────────────

  @Test
  void getAllMenusPublic_success_returns200WithList() {
    MenuDTO menu = mock(MenuDTO.class);
    when(menuService.listAllMenus()).thenReturn(List.of(menu));

    ResponseEntity<List<MenuDTO>> response = controller.getAllMenusPublic();

    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, response.getBody().size());
    verify(menuService).listAllMenus();
  }

  @Test
  void getAllMenusPublic_serviceThrows_propagatesException() {
    when(menuService.listAllMenus()).thenThrow(new RuntimeException("Service failure"));

    assertThrows(RuntimeException.class, () -> controller.getAllMenusPublic());
    verify(menuService).listAllMenus();
  }

  // ─── GET /plato ──────────────────────────────────────────────────────────

  @Test
  void getPlato_success_returnsDishList() {
    DishDto dish = mock(DishDto.class);
    when(dishService.getDish(authentication, "pasta")).thenReturn(List.of(dish));

    List<DishDto> result = controller.getPlato(authentication, "pasta");

    assertEquals(1, result.size());
    verify(dishService).getDish(authentication, "pasta");
  }

  @Test
  void getPlato_serviceThrows_propagatesException() {
    when(dishService.getDish(authentication, null)).thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class, () -> controller.getPlato(authentication, null));
    verify(dishService).getDish(authentication, null);
  }

  // ─── POST /plato ─────────────────────────────────────────────────────────

  @Test
  void createPlato_success_returns201() {
    DishCReqDto req = mock(DishCReqDto.class);
    doNothing().when(dishService).createDish(authentication, req);

    ResponseEntity<DishDto> response = controller.createPlato(authentication, req);

    assertEquals(201, response.getStatusCode().value());
    verify(dishService).createDish(authentication, req);
  }

  @Test
  void createPlato_serviceThrows_propagatesException() {
    DishCReqDto req = mock(DishCReqDto.class);
    doThrow(new IllegalArgumentException("Invalid dish")).when(dishService).createDish(authentication, req);

    assertThrows(IllegalArgumentException.class, () -> controller.createPlato(authentication, req));
    verify(dishService).createDish(authentication, req);
  }

  // ─── DELETE /plato ───────────────────────────────────────────────────────

  @Test
  void deletePlato_success_returns204() {
    doNothing().when(dishService).deleteDish(authentication, 1L, 2L);

    ResponseEntity<Void> response = controller.deletePlato(authentication, 1L, 2L);

    assertEquals(204, response.getStatusCode().value());
    verify(dishService).deleteDish(authentication, 1L, 2L);
  }

  @Test
  void deletePlato_serviceThrows_propagatesException() {
    doThrow(new NoSuchElementException("Dish not found")).when(dishService).deleteDish(authentication, 1L, 99L);

    assertThrows(NoSuchElementException.class, () -> controller.deletePlato(authentication, 1L, 99L));
    verify(dishService).deleteDish(authentication, 1L, 99L);
  }

  // ─── PATCH /plato ────────────────────────────────────────────────────────

  @Test
  void patchPlato_success_returns200WithUpdatedDish() {
    DishUReqDto req = mock(DishUReqDto.class);
    DishDto updated = mock(DishDto.class);
    when(dishService.updateDish(authentication, req)).thenReturn(updated);

    ResponseEntity<DishDto> response = controller.patchPlato(authentication, req);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(updated, response.getBody());
    verify(dishService).updateDish(authentication, req);
  }

  @Test
  void patchPlato_serviceThrows_propagatesException() {
    DishUReqDto req = mock(DishUReqDto.class);
    when(dishService.updateDish(authentication, req)).thenThrow(new RuntimeException("Update failed"));

    assertThrows(RuntimeException.class, () -> controller.patchPlato(authentication, req));
    verify(dishService).updateDish(authentication, req);
  }

  // ─── GET /search ─────────────────────────────────────────────────────────

  @Test
  void searchChefs_success_returns200WithResult() {
    ChefSearchResultDto resultDto  = mock(ChefSearchResultDto.class);
    LocalDate date                 = LocalDate.of(2025, 6, 15);
    BigDecimal min                 = BigDecimal.valueOf(50);
    BigDecimal max                 = BigDecimal.valueOf(200);
    List<String> allergens         = List.of("gluten");

    when(chefSearchService.search("italian", date, min, max, 4, allergens)).thenReturn(resultDto);

    ResponseEntity<ChefSearchResultDto> response =
      controller.searchChefs("italian", date, min, max, 4, allergens);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(resultDto, response.getBody());
    verify(chefSearchService).search("italian", date, min, max, 4, allergens);
  }

  @Test
  void searchChefs_serviceThrows_propagatesException() {
    when(chefSearchService.search(null, null, null, null, null, null))
      .thenThrow(new RuntimeException("Search failed"));

    assertThrows(RuntimeException.class,
      () -> controller.searchChefs(null, null, null, null, null, null));
    verify(chefSearchService).search(null, null, null, null, null, null);
  }

  // ─── GET /{chefId}/profile ───────────────────────────────────────────────

  @Test
  void getChefPublicProfile_success_returns200WithDto() {
    ChefPublicDetailDto dto = mock(ChefPublicDetailDto.class);
    when(chefProfileService.getChefPublicProfile(1L)).thenReturn(dto);

    ResponseEntity<ChefPublicDetailDto> response = controller.getChefPublicProfile(1L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(dto, response.getBody());
    verify(chefProfileService).getChefPublicProfile(1L);
  }

  @Test
  void getChefPublicProfile_notFound_returns404() {
    when(chefProfileService.getChefPublicProfile(99L)).thenThrow(new NoSuchElementException());

    ResponseEntity<ChefPublicDetailDto> response = controller.getChefPublicProfile(99L);

    assertEquals(404, response.getStatusCode().value());
    verify(chefProfileService).getChefPublicProfile(99L);
  }

  // ─── GET /menus/{menuId}/public ──────────────────────────────────────────

  @Test
  void getMenuPublicDetail_success_returns200WithDto() {
    MenuPublicDetailDto dto = mock(MenuPublicDetailDto.class);
    when(chefProfileService.getMenuPublicDetail(5L)).thenReturn(dto);

    ResponseEntity<MenuPublicDetailDto> response = controller.getMenuPublicDetail(5L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(dto, response.getBody());
    verify(chefProfileService).getMenuPublicDetail(5L);
  }

  @Test
  void getMenuPublicDetail_notFound_returns404() {
    when(chefProfileService.getMenuPublicDetail(999L)).thenThrow(new NoSuchElementException());

    ResponseEntity<MenuPublicDetailDto> response = controller.getMenuPublicDetail(999L);

    assertEquals(404, response.getStatusCode().value());
    verify(chefProfileService).getMenuPublicDetail(999L);
  }

  // ─── PATCH /profile ──────────────────────────────────────────────────────

  @Test
  void updateChefProfile_success_returns200WithUpdatedProfile() {
    ChefUReqDto req              = mock(ChefUReqDto.class);
    ChefPublicDetailDto updated  = mock(ChefPublicDetailDto.class);
    when(chefProfileService.updateChefProfile(authentication, req)).thenReturn(updated);

    ResponseEntity<ChefPublicDetailDto> response = controller.updateChefProfile(authentication, req);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(updated, response.getBody());
    verify(chefProfileService).updateChefProfile(authentication, req);
  }

  @Test
  void updateChefProfile_serviceThrows_propagatesException() {
    ChefUReqDto req = mock(ChefUReqDto.class);
    when(chefProfileService.updateChefProfile(authentication, req))
      .thenThrow(new RuntimeException("Profile update failed"));

    assertThrows(RuntimeException.class, () -> controller.updateChefProfile(authentication, req));
    verify(chefProfileService).updateChefProfile(authentication, req);
  }

  // ─── POST /profile/photo ─────────────────────────────────────────────────

  @Test
  void uploadPhoto_success_returns200WithBase64() {
    MultipartFile file = mock(MultipartFile.class);
    when(authentication.getName()).thenReturn("chef@example.com");
    when(file.getOriginalFilename()).thenReturn("photo.jpg");
    when(file.getSize()).thenReturn(1024L);
    when(photoUploadService.uploadChefPhoto(file, authentication)).thenReturn("base64encoded==");

    ResponseEntity<Map<String, String>> response = controller.uploadPhoto(file, authentication);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("base64encoded==", response.getBody().get("photo"));
    verify(photoUploadService).uploadChefPhoto(file, authentication);
  }

  @Test
  void uploadPhoto_serviceThrows_propagatesException() {
    MultipartFile file = mock(MultipartFile.class);
    when(authentication.getName()).thenReturn("chef@example.com");
    when(file.getOriginalFilename()).thenReturn("photo.jpg");
    when(file.getSize()).thenReturn(1024L);
    when(photoUploadService.uploadChefPhoto(file, authentication))
      .thenThrow(new RuntimeException("Upload failed"));

    assertThrows(RuntimeException.class, () -> controller.uploadPhoto(file, authentication));
    verify(photoUploadService).uploadChefPhoto(file, authentication);
  }

  // ─── POST /profile/cover-photo ───────────────────────────────────────────

  @Test
  void uploadCoverPhoto_success_returns200WithBase64() {
    MultipartFile file = mock(MultipartFile.class);
    when(photoUploadService.uploadChefCoverPhoto(file, authentication)).thenReturn("coverBase64==");

    ResponseEntity<Map<String, String>> response = controller.uploadCoverPhoto(file, authentication);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("coverBase64==", response.getBody().get("coverPhoto"));
    verify(photoUploadService).uploadChefCoverPhoto(file, authentication);
  }

  @Test
  void uploadCoverPhoto_serviceThrows_propagatesException() {
    MultipartFile file = mock(MultipartFile.class);
    when(photoUploadService.uploadChefCoverPhoto(file, authentication))
      .thenThrow(new RuntimeException("Cover upload failed"));

    assertThrows(RuntimeException.class, () -> controller.uploadCoverPhoto(file, authentication));
    verify(photoUploadService).uploadChefCoverPhoto(file, authentication);
  }
}
