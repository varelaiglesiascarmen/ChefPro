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
    this.errorMessage = 'Usuario o contraseña obligatorios.';
    return;
  }

  this.isLoading = true;

  this.authService.login(this.loginData).subscribe({
    next: (user: any) => { 
      this.isLoading = false;
      // AuthService ya guarda el token y la sesión (setSession).
      // Aquí solo navegamos tras el login correcto.
      this.router.navigate(['/user-menu']);
    },
    error: (err) => {
      this.isLoading = false;
      if (err.status === 401 || err.status === 403) {
        this.errorMessage = 'Credenciales inválidas.';
      } else {
        this.errorMessage = 'Error de conexión con el servidor.';
      }
    }
  });
}
}
