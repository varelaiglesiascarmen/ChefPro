import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ChefService } from '../../services/chef.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  private authService = inject(AuthService);
  private chefService = inject(ChefService);
  private router = inject(Router);

  // Estado de la UI
  user: any = null;
  role: 'CHEF' | 'DINER' | 'ADMIN' | null = null;
  activeTab: 'info' | 'menus' | 'calendar' | 'orders' = 'info';
  editMode = false;

  // Datos específicos para el Chef (según tu DDBB)
  chefProfile: any = { bio: '', prizes: '', photo: '' };
  myMenus: any[] = [];
  reservations: any[] = []; // Todas las reservas para el calendario
  pendingOrders: any[] = []; // Solo las 'PENDING' para el buzón

  ngOnInit() {
    this.user = this.authService.currentUserValue;
    if (!this.user) {
      this.router.navigate(['/login']);
      return;
    }
    this.role = this.user.role;

    if (this.role === 'CHEF') {
      this.loadChefData();
    } else {
      this.loadDinerData();
    }
  }

  loadChefData() {
    const id = this.user.user_ID;
    // Cargamos perfil extendido (bio, premios...), menús y reservas
    this.chefService.getChefDetails(id).subscribe(data => this.chefProfile = data);
    this.chefService.getChefMenus(id).subscribe(data => this.myMenus = data);
    this.chefService.getChefReservations(id).subscribe(data => {
      this.reservations = data;
      this.pendingOrders = data.filter((r: any) => r.status === 'PENDING');
    });
  }

  loadDinerData() {
    // Lógica para el comensal (la haremos en el siguiente paso si quieres)
    console.log('Cargando vista de Comensal...');
  }

  // ACCIONES DEL CHEF
  updateStatus(reservation: any, newStatus: 'CONFIRMED' | 'REJECTED') {
    this.chefService.updateReservationStatus(reservation.chef_ID, reservation.date, newStatus)
      .subscribe(() => {
        reservation.status = newStatus;
        this.pendingOrders = this.pendingOrders.filter(o => o !== reservation);
      });
  }

  deleteMenu(menuId: number) {
    if (confirm('¿Seguro que quieres borrar este menú y todos sus platos?')) {
      this.chefService.deleteMenu(menuId).subscribe(() => {
        this.myMenus = this.myMenus.filter(m => m.menu_ID !== menuId);
      });
    }
  }
}
