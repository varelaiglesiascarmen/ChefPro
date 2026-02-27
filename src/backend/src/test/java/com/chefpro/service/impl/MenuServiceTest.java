package com.chefpro.service.impl;

import com.chefpro.backendjava.common.object.dto.MenuCReqDto;
import com.chefpro.backendjava.common.object.dto.MenuDTO;
import com.chefpro.backendjava.common.object.dto.MenuUReqDto;
import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.common.object.entity.Menu;
import com.chefpro.backendjava.common.object.entity.UserLogin;
import com.chefpro.backendjava.repository.ChefRepository;
import com.chefpro.backendjava.repository.MenuRepository;
import com.chefpro.backendjava.service.MenuService;
import com.chefpro.backendjava.service.impl.MenuServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuServiceTest {

    // Declaramos la interfaz, instanciamos el impl
    private MenuService menuService;

    private MenuRepository menuRepository;
    private ChefRepository chefRepository;
    private Authentication authentication;

    // Entidades reutilizables
    private Chef chef;
    private UserLogin userLogin;

    @BeforeEach
    void setUp() {
        menuRepository = mock(MenuRepository.class);
        chefRepository = mock(ChefRepository.class);
        authentication = mock(Authentication.class);

        menuService = new MenuServiceImpl(menuRepository, chefRepository);

        // Construir entidades base
        userLogin = mock(UserLogin.class);
        when(userLogin.getUsername()).thenReturn("chef@example.com");
        when(userLogin.getName()).thenReturn("Mario");
        when(userLogin.getLastname()).thenReturn("Rossi");

        chef = mock(Chef.class);
        when(chef.getId()).thenReturn(1L);
        when(chef.getUser()).thenReturn(userLogin);

        when(authentication.getName()).thenReturn("chef@example.com");
        when(chefRepository.findByUser_Username("chef@example.com")).thenReturn(Optional.of(chef));
    }

    // ─── createMenu ──────────────────────────────────────────────────────────

    @Test
    void createMenu_success_returnsMenuDto() {
        MenuCReqDto dto = mock(MenuCReqDto.class);
        when(dto.getTitle()).thenReturn("Menú Mediterráneo");
        when(dto.getPricePerPerson()).thenReturn(BigDecimal.valueOf(45));
        when(dto.getDescription()).thenReturn("Descripción");
        when(dto.getMinNumberDiners()).thenReturn(2);
        when(dto.getMaxNumberDiners()).thenReturn(8);
        when(dto.getKitchenRequirements()).thenReturn(null);

        Menu savedMenu = mock(Menu.class);
        when(savedMenu.getId()).thenReturn(10L);
        when(savedMenu.getTitle()).thenReturn("Menú Mediterráneo");
        when(savedMenu.getDescription()).thenReturn("Descripción");
        when(savedMenu.getPricePerPerson()).thenReturn(BigDecimal.valueOf(45));
        when(menuRepository.save(any(Menu.class))).thenReturn(savedMenu);

        MenuDTO result = menuService.createMenu(dto, authentication);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Menú Mediterráneo", result.getTitle());
        verify(chefRepository).findByUser_Username("chef@example.com");
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    void createMenu_blankTitle_throwsIllegalArgumentException() {
        MenuCReqDto dto = mock(MenuCReqDto.class);
        when(dto.getTitle()).thenReturn("  ");

        assertThrows(IllegalArgumentException.class, () -> menuService.createMenu(dto, authentication));
        verify(menuRepository, never()).save(any());
    }

    // ─── listByChef ──────────────────────────────────────────────────────────

    @Test
    void listByChef_success_returnsMenuList() {
        Menu menu = mock(Menu.class);
        when(menu.getId()).thenReturn(1L);
        when(menu.getTitle()).thenReturn("Menú Test");
        when(menu.getDishes()).thenReturn(new ArrayList<>());
        when(menu.getChef()).thenReturn(chef);
        when(menuRepository.findByChefIdWithDishes(1L)).thenReturn(List.of(menu));

        List<MenuDTO> result = menuService.listByChef(authentication);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(chefRepository).findByUser_Username("chef@example.com");
        verify(menuRepository).findByChefIdWithDishes(1L);
    }

    @Test
    void listByChef_chefNotFound_throwsRuntimeException() {
        when(chefRepository.findByUser_Username("chef@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> menuService.listByChef(authentication));
        verify(menuRepository, never()).findByChefIdWithDishes(any());
    }

    // ─── deleteMenu ──────────────────────────────────────────────────────────

    @Test
    void deleteMenu_success_deletesMenu() {
        Menu menu = mock(Menu.class);
        when(menu.getChef()).thenReturn(chef);
        when(menu.getReservations()).thenReturn(new ArrayList<>());
        when(menuRepository.findById(5L)).thenReturn(Optional.of(menu));

        menuService.deleteMenu(authentication, 5L);

        verify(menuRepository).delete(menu);
    }

    @Test
    void deleteMenu_menuNotFound_throwsRuntimeException() {
        when(menuRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> menuService.deleteMenu(authentication, 99L));
        verify(menuRepository, never()).delete(any());
    }

    // ─── updateMenu ──────────────────────────────────────────────────────────

    @Test
    void updateMenu_success_returnsUpdatedDto() {
        MenuUReqDto uReq = mock(MenuUReqDto.class);
        when(uReq.getId()).thenReturn(5L);
        when(uReq.getTitle()).thenReturn("Nuevo Título");
        when(uReq.getDescription()).thenReturn(null);
        when(uReq.getPricePerPerson()).thenReturn(BigDecimal.valueOf(60));
        when(uReq.getMinNumberDiners()).thenReturn(null);
        when(uReq.getMaxNumberDiners()).thenReturn(null);
        when(uReq.getKitchenRequirements()).thenReturn(null);

        Menu menu = mock(Menu.class);
        when(menu.getChef()).thenReturn(chef);
        when(menuRepository.findById(5L)).thenReturn(Optional.of(menu));

        Menu savedMenu = mock(Menu.class);
        when(savedMenu.getId()).thenReturn(5L);
        when(savedMenu.getTitle()).thenReturn("Nuevo Título");
        when(savedMenu.getPricePerPerson()).thenReturn(BigDecimal.valueOf(60));
        when(menuRepository.save(menu)).thenReturn(savedMenu);

        MenuDTO result = menuService.updateMenu(authentication, uReq);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        verify(menuRepository).save(menu);
    }

    @Test
    void updateMenu_nullId_throwsIllegalArgumentException() {
        MenuUReqDto uReq = mock(MenuUReqDto.class);
        when(uReq.getId()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> menuService.updateMenu(authentication, uReq));
        verify(menuRepository, never()).findById(any());
    }

    // ─── listAllMenus ────────────────────────────────────────────────────────

    @Test
    void listAllMenus_success_returnsAllMenus() {
        Menu menu = mock(Menu.class);
        when(menu.getId()).thenReturn(1L);
        when(menu.getDishes()).thenReturn(new ArrayList<>());
        when(menu.getChef()).thenReturn(chef);
        when(menuRepository.findAllWithDishes()).thenReturn(List.of(menu));

        List<MenuDTO> result = menuService.listAllMenus();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(menuRepository).findAllWithDishes();
    }

    @Test
    void listAllMenus_repositoryThrows_propagatesException() {
        when(menuRepository.findAllWithDishes()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> menuService.listAllMenus());
        verify(menuRepository).findAllWithDishes();
    }
}
