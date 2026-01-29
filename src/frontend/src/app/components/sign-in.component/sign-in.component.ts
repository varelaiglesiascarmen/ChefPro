import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RegisterRequest } from '../../models/auth.model';

@Component({
  selector: 'app-sign-in',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.css']
})
export class SignInComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  registerData: RegisterRequest = {
    name: '',
    surname: '',
    username: '',
    email: '',
    password: ''
  };

  confirmEmail: string = '';
  confirmPassword: string = '';

  // Username and Email availability status
  usernameStatus: 'PENDING' | 'AVAILABLE' | 'TAKEN' | null = null;
  emailStatus: 'PENDING' | 'AVAILABLE' | 'TAKEN' | 'INVALID_FORMAT' | null = null;

  // Password
  passwordStrength: number = 0;
  passwordFeedback: string = '';
  passwordStrengthText: string = '';

  isLoading = false;
  errorMessage = '';

  // check username
  checkUsername() {
    // Si está vacío, limpiamos estado y salimos
    if (!this.registerData.username.trim()) {
      this.usernameStatus = null;
      return;
    }

    this.usernameStatus = 'PENDING';

    this.authService.checkUsernameAvailability(this.registerData.username)
      .subscribe({
        next: (isAvailable) => {
          this.usernameStatus = isAvailable ? 'AVAILABLE' : 'TAKEN';
        },
        error: () => {
          this.usernameStatus = null; // In case of error, we clear the status.
        }
      });
  }

  // check email
  checkEmail() {
    const email = this.registerData.email;

    if (!email.trim()) {
      this.emailStatus = null;
      return;
    }

    const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailPattern.test(email)) {
      this.emailStatus = 'INVALID_FORMAT';
      return;
    }

    this.emailStatus = 'PENDING';

    this.authService.checkEmailAvailability(email)
      .subscribe({
        next: (isAvailable) => {
          this.emailStatus = isAvailable ? 'AVAILABLE' : 'TAKEN';
        },
        error: () => {
          this.emailStatus = null;
        }
      });
  }

  // strength calculator
  calculateStrength() {
    const pass = this.registerData.password;
    if (!pass) {
      this.passwordStrength = 0;
      this.passwordFeedback = '';
      this.passwordStrengthText = '';
      return;
    }

    let score = 0;
    const hasMinLength = pass.length >= 8;
    const hasNumber = /[0-9]/.test(pass);
    const hasSpecial = /[;,._!@#$]/.test(pass);

    if (hasMinLength) score++;
    if (hasNumber) score++;
    if (hasSpecial) score++;

    this.passwordStrength = score;

    // Short text to display next to the bar
    if (score === 1) this.passwordStrengthText = 'Débil';
    else if (score === 2) this.passwordStrengthText = 'Media';
    else if (score === 3) this.passwordStrengthText = 'Fuerte';
    else this.passwordStrengthText = 'Muy débil';

    if (score < 3) {
       this.passwordFeedback = 'Usa 8 caracteres, números y símbolos.';
    } else {
       this.passwordFeedback = '';
    }
  }

  onRegister() {
    if (!this.registerData.name || !this.registerData.surname || !this.registerData.username || !this.registerData.email || !this.registerData.password) {
      this.errorMessage = 'Por favor, rellena todos los campos obligatorios.';
      return;
    }

    if (this.usernameStatus === 'TAKEN' || this.emailStatus === 'TAKEN' || this.emailStatus === 'INVALID_FORMAT') {
      this.errorMessage = 'Revisa los campos marcados en rojo.';
      return;
    }

    if (this.passwordStrength < 3) {
      this.errorMessage = 'La contraseña no es segura.';
      return;
    }

    if (this.registerData.email !== this.confirmEmail || this.registerData.password !== this.confirmPassword) {
      this.errorMessage = 'Los campos de confirmación no coinciden.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register(this.registerData).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.message === 'USER_EXISTS') {
           this.errorMessage = 'El usuario o correo ya existen.';
        } else {
           this.errorMessage = 'Error en el registro.';
        }
      }
    });
  }
}
