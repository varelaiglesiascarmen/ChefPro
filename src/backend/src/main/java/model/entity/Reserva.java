package model.entity; // ← mismo package que Usuario y Menu

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Menú reservado
    @ManyToOne
    @JoinColumn(name = "Menu_id")
    private Menu menu;

    // usernames para vincular con usuarios
    private String comensalUsername;

    private String chefUsername;

    private LocalDateTime fechaReserva;

    private Integer numeroComensales;

    // PENDIENTE / CONFIRMADA / CANCELADA
    private String estado;

    // ---------- Constructores ----------

    public Reserva() {
    }

    public Reserva(Menu menu,
                   String comensalUsername,
                   String chefUsername,
                   LocalDateTime fechaReserva,
                   Integer numeroComensales,
                   String estado) {
        this.menu = menu;
        this.comensalUsername = comensalUsername;
        this.chefUsername = chefUsername;
        this.fechaReserva = fechaReserva;
        this.numeroComensales = numeroComensales;
        this.estado = estado;
    }

    // ---------- Getters y Setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public String getComensalUsername() {
        return comensalUsername;
    }

    public void setComensalUsername(String comensalUsername) {
        this.comensalUsername = comensalUsername;
    }

    public String getChefUsername() {
        return chefUsername;
    }

    public void setChefUsername(String chefUsername) {
        this.chefUsername = chefUsername;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public Integer getNumeroComensales() {
        return numeroComensales;
    }

    public void setNumeroComensales(Integer numeroComensales) {
        this.numeroComensales = numeroComensales;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
