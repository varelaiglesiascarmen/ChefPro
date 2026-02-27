import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './sidebar/sidebar.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    SidebarComponent
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  private authService = inject(AuthService);

  user: any;
  role: 'CHEF' | 'DINER' | 'ADMIN' | null = null;

  ngOnInit(): void {
    this.authService.user$.subscribe(currentUser => {
      if (currentUser) {
        this.user = currentUser;
        this.role = currentUser.role;
      }
    });
  }
}
