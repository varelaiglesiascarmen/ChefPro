package com.chefpro.backendjava.service; // ajusta a tu package real

import com.chefpro.backendjava.repository.entity.Menu;
import com.chefpro.backendjava.repository.entity.reservations;
import com.chefpro.backendjava.repository.MenuRepository;
import com.chefpro.backendjava.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final MenuRepository menuRepository;

    public ReservaService(ReservaRepository reservaRepository, MenuRepository menuRepository) {
        this.reservaRepository = reservaRepository;
        this.menuRepository = menuRepository;
    }

    /**
     * Crear una reserva para un menú concreto, hecha por un comensal.
     */
    public reservations crearReserva(Long menuId, String comensalUsername, Integer numeroComensales) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menú no encontrado con id: " + menuId));

        reservations reserva = new reservations();
        reserva.setMenu(menu);
        reserva.setComensalUsername(comensalUsername);
        //reserva.setChefUsername(menu.get());
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setNumeroComensales(numeroComensales);
        reserva.setEstado("PENDIENTE");

        return reservaRepository.save(reserva);
    }

    /**
     * Listar reservas de un comensal (por si quieres mostrarle su histórico).
     */
    public List<reservations> listarPorComensal(String comensalUsername) {
        return reservaRepository.findByComensalUsername(comensalUsername);
    }

    /**
     * (Opcional) Listar reservas de un chef.
     */
    public List<reservations> listarPorChef(String chefUsername) {
        return reservaRepository.findByChefUsername(chefUsername);
    }
}
