import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router'; // Importante para la navegación
import { SidebarComponent } from './sidebar/sidebar.component';
// Nota: Ajusta estas rutas según tu nueva estructura de carpetas en image_8fb3ea.png
import { UserInfoComponent } from './sidebar/user-info/user-info.component';
import { ChefMenusComponent } from './sidebar/chef-menus/chef-menus.component';
import { UserOrdersComponent } from './sidebar/user-orders/user-orders.component';
import { UserCalendarComponent } from './sidebar/user-calendar/user-calendar.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    SidebarComponent,
    UserInfoComponent,
    ChefMenusComponent,
    UserOrdersComponent,
    UserCalendarComponent
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  private authService = inject(AuthService);

  user: any;
  // Corregimos el tipo para incluir ADMIN
  role: 'CHEF' | 'DINER' | 'ADMIN' | null = null;

  ngOnInit(): void {
    // Usamos el valor actual del servicio
    const currentUser = this.authService.currentUserValue;
    if (currentUser) {
      this.user = currentUser;
      this.role = currentUser.role;
    }
  }
}
