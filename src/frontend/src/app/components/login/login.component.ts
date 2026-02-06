import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FocusOnInitDirective } from '../../directives/focus-on-init.directive';
import { LoginRequest } from '../../models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, FocusOnInitDirective, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  loginData: LoginRequest = { username: '', password: '' };
  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  clearError() {
    this.errorMessage = '';
  }

  onLogin() {
    this.errorMessage = '';
    if (!this.loginData.username || !this.loginData.password) {
      this.errorMessage = 'Username or password required.';
      return;
    }

    this.isLoading = true;
    this.cdr.detectChanges();

    this.authService.login(this.loginData).subscribe({
      next: (user: any) => {
        this.isLoading = false;
        this.cdr.detectChanges();
        // AuthService already saves the token and session (setSession).
        // Here we only navigate after successful login.
        this.router.navigate(['/index']);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 401 || err.status === 403) {
          this.errorMessage = 'Credenciales inválidas.';
        } else {
          this.errorMessage = 'Error de conexión con el servidor.';
        }
        this.cdr.detectChanges();
        console.error('Login error:', err);
      }
    });
  }
}
