package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.MenuDTO;
import com.chefpro.backendjava.repository.MenuRepository;
import com.chefpro.backendjava.common.object.entity.Menu;
import com.chefpro.backendjava.service.MenuService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("MenuService")
public class MenuServiceImpl implements MenuService {

    private final MenuRepository repository;

    public MenuServiceImpl(MenuRepository repository) {
        this.repository = repository;
    }

    @Override
    public void crearMenu(MenuDTO dto, Authentication authentication) {



    }

    @Override
    public List<MenuDTO> listarPorChef(Authentication authentication) {

        List<Menu> menus = repository.findByChefUsername(authentication.getName());

        List<MenuDTO> menusDtoFinalList = menus.stream()
                .map(menu -> MenuDTO.builder()
                        .id(menu.getId())
                        .title(menu.getTitle())
                        .description(menu.getDescription())
                        .dishes(menu.getDishes())
                        .allergens(menu.getAllergens())
                        .pricePerPerson(menu.getPricePerPerson())
                        .deliveryAvailable(menu.isDeliveryAvailable())
                        .cookAtClientHome(menu.isCookAtClientHome())
                        .pickupAvailable(menu.isPickupAvailable())
                        .chefUsername(menu.getChef().getName())
                        .createdAt(menu.getCreatedAt())
                        .build()
                )
                .toList();

        return menusDtoFinalList;
    }
}
