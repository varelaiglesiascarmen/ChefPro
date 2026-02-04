import { Component } from '@angular/core';
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
    private router: Router
  ) {}

  onLogin() {
    this.errorMessage = '';

    if (!this.loginData.username || !this.loginData.password) {
      this.errorMessage = 'Usuario o contraseña incorrectos.';
      return;
    }

    this.isLoading = true;

    this.authService.login(this.loginData).subscribe({
      // successful response
      next: (res) => {
        this.isLoading = false;

        if (res.success) {
          console.log('Login correcto:', res.user?.name);
          this.router.navigate(['/index']);
        } else {
          // server responds “200 OK” but with error logic (ex. incorrect password)
          this.errorMessage = res.message || 'Error al iniciar sesión';
        }
      },

      // unsuccessful response
      error: (err) => {
        this.isLoading = false;

        console.error('Error HTTP:', err.status);

        if (err.status === 401 || err.status === 403) {
          this.errorMessage = 'Usuario o contraseña incorrectos.';
        } else if (err.status === 0) {
          this.errorMessage = 'No se pudo conectar con el servidor.';
        } else if (err.status === 500) {
          this.errorMessage = 'Error interno del servidor. Inténtalo luego.';
        } else {
          this.errorMessage = 'Ocurrió un error inesperado.';
        }
      }
    });
  }
}
