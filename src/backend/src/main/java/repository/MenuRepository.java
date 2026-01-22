package repository;

import model.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    // Menús creados por un chef concreto
    List<Menu> findByChefUsername(String chefUsername);
}
