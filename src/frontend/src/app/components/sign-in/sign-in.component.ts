import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, takeUntil, filter } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { signupRequest } from '../../models/auth.model';

@Component({
  selector: 'app-sign-in',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.css']
})
export class SignInComponent implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private router = inject(Router);

  // Subjects for debounced validation
  private username$ = new Subject<string>();
  private email$ = new Subject<string>();
  private destroy$ = new Subject<void>();

  // DATA INITIALIZATION
  // We use ‘null as any’ in the role so that it starts empty and forces the user to choose.
  signupData: signupRequest = {
    name: '',
    surname: '',
    username: '',
    email: '',
    password: '',
    role: null as any
  };

  confirmEmail: string = '';
  confirmPassword: string = '';

  // Variable to control whether the user attempted to signup without choosing a role
  roleError: boolean = false;

  // Availability statuses (Asynchronous validation)
  usernameStatus: 'PENDING' | 'AVAILABLE' | 'TAKEN' | null = null;
  emailStatus: 'PENDING' | 'AVAILABLE' | 'TAKEN' | 'INVALID_FORMAT' | null = null;

  // Password strength variables
  passwordStrength: number = 0;
  passwordFeedback: string = '';
  passwordStrengthText: string = '';

  // Loading and error handling
  isLoading = false;
  errorMessage = '';

  ngOnInit() {
    // Debounced username check (300ms after user stops typing)
    this.username$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      filter(val => val.trim().length > 0),
      switchMap(username => {
        this.usernameStatus = 'PENDING';
        return this.authService.checkUsernameAvailability(username);
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (isAvailable) => {
        this.usernameStatus = isAvailable ? 'AVAILABLE' : 'TAKEN';
      },
      error: () => {
        this.usernameStatus = null;
      }
    });

    // Debounced email check (300ms after user stops typing)
    this.email$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      filter(val => val.trim().length > 0),
      switchMap(email => {
        const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailPattern.test(email)) {
          this.emailStatus = 'INVALID_FORMAT';
          return [];
        }
        this.emailStatus = 'PENDING';
        return this.authService.checkEmailAvailability(email);
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (isAvailable) => {
        this.emailStatus = isAvailable ? 'AVAILABLE' : 'TAKEN';
      },
      error: () => {
        this.emailStatus = null;
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ROLE SELECTION LOGIC (CHEF vs DINER)
  selectRole(role: 'DINER' | 'CHEF') {
    this.signupData.role = role;
    this.roleError = false;
  }

  // Reactive validation methods – push values into subjects
  onUsernameInput() {
    const val = this.signupData.username;
    if (!val.trim()) {
      this.usernameStatus = null;
      return;
    }
    this.username$.next(val);
  }

  onEmailInput() {
    const val = this.signupData.email;
    if (!val.trim()) {
      this.emailStatus = null;
      return;
    }
    this.email$.next(val);
  }

  // calculate password strength
  calculateStrength() {
    const pass = this.signupData.password;
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

  // registration method
  onsignup() {

    // Validate required fields
    if (!this.signupData.name || !this.signupData.surname || !this.signupData.username || !this.signupData.email || !this.signupData.password) {
      this.errorMessage = 'Por favor, rellena todos los campos obligatorios.';
      return;
    }

    // validate username and email availability
    if (this.usernameStatus === 'TAKEN' || this.emailStatus === 'TAKEN' || this.emailStatus === 'INVALID_FORMAT') {
      this.errorMessage = 'El nombre de usuario o correo ya existen.';
      return;
    }

    // validate that the email is the same in the inputs
    if (this.signupData.email !== this.confirmEmail) {
      this.errorMessage = 'El correo no coincide.';
      return;
    }

    // validate that the pasword is the same in the inputs
    if (this.signupData.password !== this.confirmPassword) {
      this.errorMessage = 'La contraseña no coincide.';
      return;
    }

    // validate rol strength
    if (!this.signupData.role) {
      this.roleError = true;
      this.errorMessage = 'Por favor, seleccione Comensal o Chef.';
      return;
    }

    // send to backend
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.signup(this.signupData).subscribe({
      next: () => {
        this.isLoading = false;
        // Navigate to index on successful registration
        this.router.navigate(['/index']);
      },
      error: (err) => {
        this.isLoading = false;
        // Handle specific error messages
        if (err.message === 'USER_EXISTS') {
          this.errorMessage = 'El usuario o correo ya existen.';
        } else {
          this.errorMessage = 'Error en el registro. Inténtalo de nuevo.';
        }
      }
    });
  }
}
