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
  public authService = inject(AuthService);
  public chefService = inject(ChefService);
  public router = inject(Router);

  // UI State
  user: any = null;
  role: 'CHEF' | 'DINER' | 'ADMIN' | null = null;
  activeTab: 'info' | 'menus' | 'orders' | 'calendar' = 'info';
  editMode = false;

  // Data from DDBB
  chefProfile: any = { bio: '', prizes: '', photo: '' };
  myMenus: any[] = [];
  reservations: any[] = [];
  pendingOrders: any[] = [];

  // Calendar State
  currentDate = new Date();
  calendarDays: any[] = [];

  ngOnInit() {
    this.user = this.authService.currentUserValue;
    if (!this.user) {
      this.router.navigate(['/login']);
      return;
    }
    this.role = this.user.role;

    if (this.role === 'CHEF') {
      this.loadChefData();
    }
  }

  loadChefData() {
    const id = this.user.user_ID;
    this.chefService.getChefDetails(id).subscribe(data => this.chefProfile = data);
    this.chefService.getChefMenus(id).subscribe(data => this.myMenus = data);
    this.chefService.getChefReservations(id).subscribe(data => {
      this.reservations = data;
      this.pendingOrders = data.filter((r: any) => r.status === 'PENDING');
      this.generateCalendar(); // Generamos los días una vez tenemos las fechas
    });
  }

  // --- MÉTODO OLVIDADO: GUARDAR PERFIL ---
  updateProfile() {
    // Aquí mandamos el objeto a la tabla 'chefs' y 'users'
    this.chefService.updateChef(this.user.user_ID, this.chefProfile).subscribe({
      next: () => {
        this.editMode = false;
        alert('Perfil actualizado correctamente.');
      },
      error: () => alert('Error al guardar los cambios.')
    });
  }

  // --- LÓGICA DE CALENDARIO ---
  generateCalendar() {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    this.calendarDays = [];

    // Huecos vacíos al principio
    for (let i = 0; i < (firstDay === 0 ? 6 : firstDay - 1); i++) {
      this.calendarDays.push({ day: null });
    }

    // Días del mes
    for (let d = 1; d <= daysInMonth; d++) {
      const dateStr = `${year}-${(month + 1).toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}`;

      // Buscamos si hay reserva en este día (Tabla reservations)
      const res = this.reservations.find(r => r.date === dateStr);

      this.calendarDays.push({
        day: d,
        date: dateStr,
        reservation: res
      });
    }
  }

  // --- ACCIONES DE CHEF ---
  updateStatus(reservation: any, newStatus: 'CONFIRMED' | 'REJECTED') {
    this.chefService.updateReservationStatus(reservation.chef_ID, reservation.date, newStatus)
      .subscribe(() => {
        reservation.status = newStatus;
        this.pendingOrders = this.pendingOrders.filter(o => o !== reservation);
        this.generateCalendar(); // Refrescamos el calendario
      });
  }

  deleteMenu(menuId: number) {
    if (confirm('¿Seguro que quieres borrar este menú y todos sus platos?')) {
      this.chefService.deleteMenu(menuId).subscribe(() => {
        this.myMenus = this.myMenus.filter(m => m.menu_ID !== menuId);
      });
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  loadDinerData() { console.log('Vista Diner...'); }
}
