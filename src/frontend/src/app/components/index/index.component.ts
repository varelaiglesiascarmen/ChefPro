import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-index',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './index.component.html',
  styleUrls: ['./index.component.css']
})
export class IndexComponent {
  public authService = inject(AuthService);
  private router = inject(Router);

  shouldShowChefCard(user: User | null): boolean {
    return !user || user.role === 'CHEF';
  }

  navigateTo(path: 'CHEF' | 'DINER') {
    const user = this.authService.currentUserValue;

    if (!user) {
      this.router.navigate(['/login']);
      return;
    }

    if (user.role === 'CHEF') {
      this.router.navigate(['/profile/new-menu']);
      return;
    }

    if (user.role === 'DINER') {
      this.router.navigate(['/login']);
      return;
    }

    this.router.navigate(['/login']);
  }
}
