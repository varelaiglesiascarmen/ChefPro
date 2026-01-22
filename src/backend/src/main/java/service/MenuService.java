package service;
import model.entity.Menu;
import repository.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    /**
     * Crear un nuevo menú asociado a un chef.
     */
    public Menu crearMenu(String nombre, String descripcion, Double precio, String chefUsername) {
        Menu menu = new Menu();
        menu.setNombre(nombre);
        menu.setDescripcion(descripcion);
        menu.setPrecio(precio);
        menu.setChefUsername(chefUsername);

        return menuRepository.save(menu);
    }

    /**
     * Listar todos los menús disponibles (para que el comensal los vea).
     */
    public List<Menu> listarTodos() {
        return menuRepository.findAll();
    }

    /**
     * Listar los menús creados por un chef concreto (para la vista del chef).
     */
    public List<Menu> listarPorChef(String chefUsername) {
        return menuRepository.findByChefUsername(chefUsername);
    }
}
