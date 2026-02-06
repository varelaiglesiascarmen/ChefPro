import { Component, inject, OnInit } from '@angular/core';
import { ReservationService } from '../../../../services/reservation.service';
import { AuthService } from '../../../../services/auth.service';
import { CommonModule, DatePipe, NgClass } from '@angular/common';

@Component({
  selector: 'app-user-orders.component',
  imports: [CommonModule, DatePipe, NgClass],
  templateUrl: './user-orders.component.html',
  styleUrl: './user-orders.component.css',
})
export class UserOrdersComponent implements OnInit {
  private resService = inject(ReservationService);
  private authService = inject(AuthService);

  orders: any[] = [];
  role: string | null = null;

  ngOnInit() {
    this.authService.user$.subscribe(user => {
      this.role = user?.role || null;
      this.loadOrders();
    });
  }

  loadOrders() {
    this.resService.getReservations().subscribe((data: any[]) => {
      this.orders = data;
    });
  }

  handleRequest(id: number, newStatus: 'ACCEPTED' | 'REJECTED') {
    this.resService.updateStatus(id, newStatus).subscribe({
      next: () => {
        alert(newStatus === 'ACCEPTED' ? 'Reserva aceptada. ¡A la cocina!' : 'Reserva rechazada.');
        this.loadOrders();
      },
      error: () => alert('Error al procesar la solicitud.')
    });
  }
}
