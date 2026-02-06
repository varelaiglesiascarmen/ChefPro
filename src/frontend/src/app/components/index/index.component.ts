import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-index',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './index.component.html',
  styleUrls: ['./index.component.css']
})
export class IndexComponent {
  public authService = inject(AuthService);
  private router = inject(Router);

  navigateTo(path: 'CHEF' | 'DINER') {
    const user = this.authService.currentUserValue;

    if (!user) {
      this.router.navigate(['/signup'], { queryParams: { role: path } });
    } else {
      this.router.navigate(['/profile']);
    }
  }
}
