// import necessary Angular modules and services
import { Router } from '@angular/router';
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { FocusOnInitDirective } from '../../directives/focus-on-init.directive';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, FocusOnInitDirective],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginData = { username: '', password: '' };

  constructor(private authService: AuthService) {}

  onLogin() {
    this.authService.login(this.loginData).subscribe({
      next: (res) => console.log('Login ok', res),
      error: (err) => alert('Error: ' + err.error)
    });
  }
}
