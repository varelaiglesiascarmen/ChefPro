import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../../services/auth.service';

export interface Order {
  id: string;
  chefId: number;
  dateRaw: string;
  dinerName: string;
  date: string;
  menuName: string;
  guests: number;
  price: number;
  status: 'PENDING' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED';
  location: string;
}

export interface ReservationApi {
  chefId: number;
  date: string;
  dinerId: number;
  menuId: number;
  numberOfDiners: number;
  address: string;
  status: 'PENDING' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED';
  chefName: string;
  dinerName: string;
  menuTitle: string;
}

@Component({
  selector: 'app-user-orders',
  imports: [CommonModule],
  templateUrl: './user-orders.component.html',
  styleUrls: ['./user-orders.component.css']
})
export class UserOrdersComponent implements OnInit {
  private authService = inject(AuthService);
  private http = inject(HttpClient);

  private apiUrl = 'http://localhost:18080/api/reservations';

  activeTab: 'PENDING' | 'CONFIRMED' = 'PENDING';
  isLoading = true;
  orders: Order[] = [];

  ngOnInit() {
    this.authService.user$.subscribe(user => {
      if (user && user.role === 'CHEF') {
        this.fetchOrders();
      } else {
        this.isLoading = false;
      }
    });
  }

  fetchOrders() {
    this.isLoading = true;

    this.http.get<ReservationApi[]>(`${this.apiUrl}/chef`).subscribe({
      next: (data) => {
        this.orders = data.map((reservation) => this.toOrder(reservation));
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error al cargar las reservas desde la API:', err);
        this.isLoading = false;
        alert('Hubo un problema de conexión al cargar tu agenda.');
      }
    });
  }

  get pendingOrders() {
    return this.orders.filter(o => o.status === 'PENDING');
  }

  get confirmedOrders() {
    return this.orders.filter(o => o.status === 'CONFIRMED');
  }

  setTab(tab: 'PENDING' | 'CONFIRMED') {
    this.activeTab = tab;
  }

  acceptOrder(order: Order) {
    const payload = { chefId: order.chefId, date: order.dateRaw, status: 'CONFIRMED' };
    this.http.patch<ReservationApi>(`${this.apiUrl}/status`, payload).subscribe({
      next: () => {
        order.status = 'CONFIRMED';
      },
      error: (err) => {
        console.error('Fallo al confirmar la reserva:', err);
        alert('No se pudo confirmar la reserva. Inténtalo de nuevo.');
      }
    });
  }

  rejectOrder(order: Order) {
    const confirmacion = confirm('¿Estás seguro de que deseas rechazar esta solicitud? El comensal será notificado.');
    if (confirmacion) {
      const payload = { chefId: order.chefId, date: order.dateRaw, status: 'REJECTED' };
      this.http.patch<ReservationApi>(`${this.apiUrl}/status`, payload).subscribe({
        next: () => {
          this.orders = this.orders.filter(o => o.id !== order.id);
        },
        error: (err) => console.error('Error al rechazar:', err)
      });
    }
  }

  cancelOrder(order: Order) {
    const confirmacion = confirm('ATENCIÓN: Cancelar una reserva confirmada puede afectar a tu reputación en Chef Pro. ¿Deseas continuar?');
    if (confirmacion) {
      const payload = { chefId: order.chefId, date: order.dateRaw, status: 'CANCELLED' };
      this.http.patch<ReservationApi>(`${this.apiUrl}/status`, payload).subscribe({
        next: () => {
          this.orders = this.orders.filter(o => o.id !== order.id);
        },
        error: (err) => console.error('Error al cancelar:', err)
      });
    }
  }

  private toOrder(reservation: ReservationApi): Order {
    return {
      id: `CHEF-${reservation.chefId}-${reservation.date}`,
      chefId: reservation.chefId,
      dateRaw: reservation.date,
      dinerName: reservation.dinerName || 'Comensal',
      date: reservation.date,
      menuName: reservation.menuTitle || 'Menu',
      guests: reservation.numberOfDiners || 0,
      price: 0,
      status: reservation.status,
      location: reservation.address || ''
    };
  }
}
