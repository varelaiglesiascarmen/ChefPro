import { Component, EventEmitter, inject, Output } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-menu',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-menu.component.html',
  styleUrl: './user-menu.component.css'
})
export class UserMenuComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  @Output() closeMenu = new EventEmitter<void>();

  // Variable para controlar la visibilidad del Modal
  showLogoutModal = false;
  showGoodbyeModal = false;

  navigateTo(route: string) {
    this.router.navigate([route]);
    this.closeMenu.emit();
  }

  // PASO 1: El usuario hace clic en "Cerrar sesión" en el menú
  onLogoutClick() {
    this.showLogoutModal = true;
  }

  // PASO 2: El usuario confirma en el modal
  confirmLogout() {
    // 1. Ocultamos el modal de pregunta
    this.showLogoutModal = false;

    // 2. Mostramos el modal de despedida
    this.showGoodbyeModal = true;

    // 3. Esperamos 2 segundos (2000ms) para que el usuario lo lea y sienta la calidez
    setTimeout(() => {
      this.authService.logout(); // Logout real (borra datos y redirige)
      this.showGoodbyeModal = false; // Limpiamos
      this.closeMenu.emit(); // Cerramos el menú
    }, 2000);
  }

  // PASO 3: El usuario cancela
  cancelLogout() {
    this.showLogoutModal = false;
    this.closeMenu.emit();
  }
}
