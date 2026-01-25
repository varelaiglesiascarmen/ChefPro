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

  // Variable to control the visibility of the Modal
  showLogoutModal = false;
  showGoodbyeModal = false;

  navigateTo(route: string) {
    this.router.navigate([route]);
    this.closeMenu.emit();
  }

  onLogoutClick() {
    this.showLogoutModal = true;
  }

  confirmLogout() {
    this.showLogoutModal = false;
    this.showGoodbyeModal = true;

    setTimeout(() => {
      this.authService.logout();
      this.showGoodbyeModal = false;
      this.closeMenu.emit();
    }, 2000);
  }

  cancelLogout() {
    this.showLogoutModal = false;
    this.closeMenu.emit();
  }
}
