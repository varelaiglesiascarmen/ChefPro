import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../../services/auth.service';
import { ToastService } from '../../../../services/toast.service';
import { environment } from '../../../../../environments/environment';
import { ReviewFormComponent } from '../../../review-form/review-form.component';

export interface Order {
  id: string;
  chefId: number;
  dateRaw: string;
  dinerName: string;
  date: string;
  menuName: string;
  guests: number;
  price: number;
  status: 'PENDING' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';
  location: string;
}

export interface ReservationApi {
  chefId: number;
  date: string;
  dinerId: number;
  menuId: number;
  numberOfDiners: number;
  address: string;
  status: 'PENDING' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';
  chefName: string;
  dinerName: string;
  menuTitle: string;
}

@Component({
  selector: 'app-user-orders',
  standalone: true,
  imports: [CommonModule, ReviewFormComponent],
  templateUrl: './user-orders.component.html',
  styleUrls: ['./user-orders.component.css']
})
export class UserOrdersComponent implements OnInit {
  private authService = inject(AuthService);
  private http = inject(HttpClient);
  private toastService = inject(ToastService);
  private cdr = inject(ChangeDetectorRef);

  activeTab: 'PENDING' | 'CONFIRMED' | 'COMPLETED' = 'PENDING';
  isLoading = true;
  orders: Order[] = [];
  userRole: string | null = null;

  // Para reviews
  reviewModalOpen = false;
  selectedReservationForReview: Order | null = null;

  // Para confirmaciones
  confirmActionType: 'reject' | 'cancel' | null = null;
  selectedOrderForConfirmation: Order | null = null;

  ngOnInit() {
    this.authService.user$.subscribe(user => {
      if (user) {
        this.userRole = user.role;
        this.fetchOrders();
      } else {
        this.isLoading = false;
      }
    });
  }

  fetchOrders() {
    this.isLoading = true;
    const url = this.userRole === 'CHEF'
      ? `${environment.apiUrl}/reservations/chef`
      : `${environment.apiUrl}/reservations/comensal`;

    this.http.get<ReservationApi[]>(url).subscribe({
      next: (data) => {
        this.orders = data.map((reservation) => this.toOrder(reservation));
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar las reservas:', err);
        this.toastService.error('No pudimos cargar tus reservas. Intenta nuevamente.');
        this.isLoading = false;
      }
    });
  }

  get pendingOrders() {
    return this.orders.filter(o => o.status === 'PENDING');
  }

  get confirmedOrders() {
    return this.orders.filter(o => o.status === 'CONFIRMED');
  }

  get completedOrders() {
    return this.orders.filter(o => o.status === 'COMPLETED');
  }

  setTab(tab: 'PENDING' | 'CONFIRMED' | 'COMPLETED') {
    this.activeTab = tab;
  }

  acceptOrder(order: Order) {
    const payload = { chefId: order.chefId, date: order.dateRaw, status: 'CONFIRMED' };
    this.http.patch<ReservationApi>(`${environment.apiUrl}/reservations/status`, payload).subscribe({
      next: () => {
        order.status = 'CONFIRMED';
        this.toastService.success('Reserva aceptada correctamente');
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al confirmar la reserva:', err);
      }
    });
  }

  rejectOrder(order: Order) {
    const payload = { chefId: order.chefId, date: order.dateRaw, status: 'REJECTED' };
    this.http.patch<ReservationApi>(`${environment.apiUrl}/reservations/status`, payload).subscribe({
      next: () => {
        this.orders = this.orders.filter(o => o.id !== order.id);
        this.toastService.success('Reserva rechazada');
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al rechazar:', err);
      }
    });
  }

  showRejectConfirmation(order: Order) {
    this.selectedOrderForConfirmation = order;
    this.confirmActionType = 'reject';
  }

  confirmReject() {
    if (this.selectedOrderForConfirmation) {
      this.rejectOrder(this.selectedOrderForConfirmation);
      this.closeConfirmation();
    }
  }

  cancelOrder(order: Order) {
    const payload = { chefId: order.chefId, date: order.dateRaw, status: 'CANCELLED' };
    this.http.patch<ReservationApi>(`${environment.apiUrl}/reservations/status`, payload).subscribe({
      next: () => {
        this.orders = this.orders.filter(o => o.id !== order.id);
        this.toastService.success('Reserva cancelada');
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cancelar:', err);
      }
    });
  }

  showCancelConfirmation(order: Order) {
    this.selectedOrderForConfirmation = order;
    this.confirmActionType = 'cancel';
  }

  confirmCancel() {
    if (this.selectedOrderForConfirmation) {
      this.cancelOrder(this.selectedOrderForConfirmation);
      this.closeConfirmation();
    }
  }

  closeConfirmation() {
    this.confirmActionType = null;
    this.selectedOrderForConfirmation = null;
  }

  openReviewModal(order: Order) {
    if (this.userRole !== 'DINER') {
      this.toastService.error('Solo los comensales pueden dejar valoraciones');
      return;
    }
    if (order.status !== 'COMPLETED') {
      this.toastService.warning('Solo puedes valorar reservas completadas');
      return;
    }
    this.selectedReservationForReview = order;
    this.reviewModalOpen = true;
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'PENDING': 'Pendiente',
      'CONFIRMED': 'Confirmada',
      'REJECTED': 'Rechazada',
      'CANCELLED': 'Cancelada',
      'COMPLETED': 'Completada'
    };
    return labels[status] || status;
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'PENDING': '#f59e0b',
      'CONFIRMED': '#10b981',
      'REJECTED': '#ef4444',
      'CANCELLED': '#9ca3af',
      'COMPLETED': '#3b82f6'
    };
    return colors[status] || '#999';
  }

  private toOrder(reservation: ReservationApi): Order {
    return {
      id: `${reservation.chefId}-${reservation.date}`,
      chefId: reservation.chefId,
      dateRaw: reservation.date,
      dinerName: reservation.dinerName || 'Comensal',
      date: reservation.date,
      menuName: reservation.menuTitle || 'Menú',
      guests: reservation.numberOfDiners || 0,
      price: 0,
      status: reservation.status as any,
      location: reservation.address || ''
    };
  }
}

