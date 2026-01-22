package service; // ajusta a tu package real

import model.entity.Menu;
import model.entity.Reserva;
import repository.MenuRepository;
import repository.ReservaRepository;
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
    public Reserva crearReserva(Long menuId, String comensalUsername, Integer numeroComensales) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menú no encontrado con id: " + menuId));

        Reserva reserva = new Reserva();
        reserva.setMenu(menu);
        reserva.setComensalUsername(comensalUsername);
        reserva.setChefUsername(menu.getChefUsername());
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setNumeroComensales(numeroComensales);
        reserva.setEstado("PENDIENTE");

        return reservaRepository.save(reserva);
    }

    /**
     * Listar reservas de un comensal (por si quieres mostrarle su histórico).
     */
    public List<Reserva> listarPorComensal(String comensalUsername) {
        return reservaRepository.findByComensalUsername(comensalUsername);
    }

    /**
     * (Opcional) Listar reservas de un chef.
     */
    public List<Reserva> listarPorChef(String chefUsername) {
        return reservaRepository.findByChefUsername(chefUsername);
    }
}
