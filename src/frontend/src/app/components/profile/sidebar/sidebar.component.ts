import { Component, OnInit, OnDestroy, inject, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private router = inject(Router);
  private userSub?: Subscription;

  @Input() user: any;
  @Input() role: string | null = null;

  ngOnInit(): void {
    this.userSub = this.authService.user$.subscribe(userData => {
      this.user = userData;
      this.role = userData?.role || null;
    });
  }

  ngOnDestroy(): void {
    this.userSub?.unsubscribe();
  }

  handleImageError(event: any): void {
    event.target.src = '/logos/users.svg';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/index']);
    console.log('Sesión cerrada correctamente');
  }
}
