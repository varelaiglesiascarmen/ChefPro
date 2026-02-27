package com.chefpro.service.impl;

import com.chefpro.backendjava.common.object.dto.ChefSearchResultDto;
import com.chefpro.backendjava.repository.ChefSearchRepository;
import com.chefpro.backendjava.repository.MenuSearchRepository;
import com.chefpro.backendjava.service.ChefSearchService;
import com.chefpro.backendjava.service.impl.ChefSearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChefSearchServiceTest {

    private ChefSearchService chefSearchService;

    private ChefSearchRepository chefSearchRepository;
    private MenuSearchRepository menuSearchRepository;

    @BeforeEach
    void setUp() {
        chefSearchRepository = mock(ChefSearchRepository.class);
        menuSearchRepository = mock(MenuSearchRepository.class);

        chefSearchService = new ChefSearchServiceImpl(chefSearchRepository, menuSearchRepository);
    }

    // ─── search — con resultados ──────────────────────────────────────────────

    @Test
    void search_withResults_returnsChefAndMenuList() {
        // Proyecciones de chef y menú mockeadas
        var chefProj = mockChefProjection();
        var menuProj = mockMenuProjection();

        when(chefSearchRepository.searchChefs("italiana", null)).thenReturn(List.of(chefProj));
        when(menuSearchRepository.searchMenus("italiana", null, null, null, null, null))
            .thenReturn(List.of(menuProj));

        ChefSearchResultDto result = chefSearchService.search(
            "italiana", null, null, null, null, null
        );

        assertNotNull(result);
        assertFalse(result.isNoResults());
        assertEquals(1, result.getChefs().size());
        assertEquals(1, result.getMenus().size());
        verify(chefSearchRepository).searchChefs("italiana", null);
        verify(menuSearchRepository).searchMenus("italiana", null, null, null, null, null);
    }

    @Test
    void search_noResultsWithQuery_returnsRandomSuggestions() {
        var randomMenu = mockMenuProjection();

        when(chefSearchRepository.searchChefs("inexistente", null)).thenReturn(List.of());
        when(menuSearchRepository.searchMenus("inexistente", null, null, null, null, null))
            .thenReturn(List.of());
        when(menuSearchRepository.findRandomMenuSuggestions(6)).thenReturn(List.of(randomMenu));

        ChefSearchResultDto result = chefSearchService.search(
            "inexistente", null, null, null, null, null
        );

        assertNotNull(result);
        assertTrue(result.isNoResults());
        assertEquals(0, result.getChefs().size());
        assertEquals(1, result.getMenus().size()); // sugerencia aleatoria
        verify(menuSearchRepository).findRandomMenuSuggestions(6);
    }

    // ─── search — con todos los filtros ──────────────────────────────────────

    @Test
    void search_withAllFilters_passesFiltersToRepositories() {
        LocalDate date         = LocalDate.of(2025, 9, 15);
        BigDecimal min         = BigDecimal.valueOf(30);
        BigDecimal max         = BigDecimal.valueOf(100);
        List<String> allergens = List.of("gluten");

        when(chefSearchRepository.searchChefs("sushi", date)).thenReturn(List.of());
        when(menuSearchRepository.searchMenus("sushi", date, min, max, 4, allergens))
            .thenReturn(List.of());
        when(menuSearchRepository.findRandomMenuSuggestions(6)).thenReturn(List.of());

        ChefSearchResultDto result = chefSearchService.search("sushi", date, min, max, 4, allergens);

        assertTrue(result.isNoResults());
        verify(chefSearchRepository).searchChefs("sushi", date);
        verify(menuSearchRepository).searchMenus("sushi", date, min, max, 4, allergens);
    }

    @Test
    void search_repositoryThrows_propagatesException() {
        when(chefSearchRepository.searchChefs(any(), any()))
            .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
            () -> chefSearchService.search("italiana", null, null, null, null, null));
        verify(chefSearchRepository).searchChefs("italiana", null);
        verifyNoInteractions(menuSearchRepository);
    }

    // ─── search — query nula o blank (sin sugerencias aleatorias) ────────────

    @Test
    void search_nullQuery_returnsEmptyResultsWithoutSuggestions() {
        when(chefSearchRepository.searchChefs(null, null)).thenReturn(List.of());
        when(menuSearchRepository.searchMenus(null, null, null, null, null, null))
            .thenReturn(List.of());

        ChefSearchResultDto result = chefSearchService.search(null, null, null, null, null, null);

        // Con query null no se deben pedir sugerencias aleatorias
        assertFalse(result.isNoResults());
        verify(menuSearchRepository, never()).findRandomMenuSuggestions(anyInt());
    }

    @Test
    void search_blankQuery_treatedAsNullNoSuggestions() {
        when(chefSearchRepository.searchChefs(null, null)).thenReturn(List.of());
        when(menuSearchRepository.searchMenus(null, null, null, null, null, null))
            .thenReturn(List.of());

        ChefSearchResultDto result = chefSearchService.search("   ", null, null, null, null, null);

        assertFalse(result.isNoResults());
        verify(menuSearchRepository, never()).findRandomMenuSuggestions(anyInt());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private com.chefpro.backendjava.common.object.dto.ChefSearchProjection mockChefProjection() {
        var p = mock(com.chefpro.backendjava.common.object.dto.ChefSearchProjection.class);
        when(p.getUserId()).thenReturn(1L);
        when(p.getUsername()).thenReturn("chef@example.com");
        when(p.getName()).thenReturn("Mario");
        when(p.getLastname()).thenReturn("Rossi");
        when(p.getPhoto()).thenReturn(null);
        when(p.getBio()).thenReturn("Bio");
        when(p.getLocation()).thenReturn("Madrid");
        when(p.getAvgScore()).thenReturn(4.5);
        when(p.getReviewsCount()).thenReturn(10L);
        when(p.getStartingPrice()).thenReturn(java.math.BigDecimal.valueOf(35));
        return p;
    }

    private com.chefpro.backendjava.common.object.dto.MenuSearchProjection mockMenuProjection() {
        var p = mock(com.chefpro.backendjava.common.object.dto.MenuSearchProjection.class);
        when(p.getMenuId()).thenReturn(1L);
        when(p.getMenuTitle()).thenReturn("Menú Test");
        when(p.getMenuDescription()).thenReturn("Descripción");
        when(p.getPricePerPerson()).thenReturn(java.math.BigDecimal.valueOf(40));
        when(p.getMinDiners()).thenReturn(2);
        when(p.getMaxDiners()).thenReturn(8);
        when(p.getChefId()).thenReturn(1L);
        when(p.getChefName()).thenReturn("Mario");
        when(p.getChefLastname()).thenReturn("Rossi");
        when(p.getChefPhoto()).thenReturn(null);
        when(p.getChefLocation()).thenReturn("Madrid");
        when(p.getAvgScore()).thenReturn(4.5);
        when(p.getReviewsCount()).thenReturn(10L);
        return p;
    }
}
