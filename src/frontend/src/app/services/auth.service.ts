import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { delay, tap } from 'rxjs/operators';
import { LoginRequest, LoginResponse, User } from '../models/auth.model';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private router = inject(Router);

  /*     ------  REAL CODE -----------

    private http = inject(HttpClient); // Importar HttpClient arriba
    private apiUrl = 'http://localhost:8080/api/auth';

  */

  // STATE MANAGEMENT (Reactivity)
  // BehaviorSubject stores the current state and emits it to anyone who subscribes
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public user$ = this.currentUserSubject.asObservable();

  constructor() {
    // attempt to rescue the saved session
    this.restoreSession();
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {

    // --------------- mock data in -----------------
    console.log('[AuthService] Intentando login con:', credentials.username);

    // mock user > ChefMaria
    // mock password > 123456

    const mockUser: User = {
      id: 'u-' + Math.floor(Math.random() * 1000),
      username: credentials.username,
      name: credentials.username,
      email: `${credentials.username}@chefpro.com`,
      role: 'CLIENT',
      photoUrl: `https://i.pravatar.cc/300?u=${credentials.username}`,

      // data for your recommendation algorithm
      preferences: {
        dietary: ['Sin Gluten', 'Bajo en carbohidratos'],
        favoriteCuisines: ['Mediterránea', 'Japonesa'],
        location: 'Madrid Centro'
      }
    };

    // simulated responses
    const successResponse: LoginResponse = {
      success: true,
      token: 'fake-jwt-token-simulado-xyz-123',
      user: mockUser
    };

    const errorResponse: LoginResponse = {
      success: false,
      message: 'Usuario o contraseña incorrectos'
    };

    // logic simulated
    if (credentials.password === '123456') {
      return of(successResponse).pipe(
        delay(1000),
        tap(response => {
          if (response.success && response.user && response.token) {
            this.setSession(response.user, response.token);
          }
        })
      );
    } else {
      return of(errorResponse).pipe(delay(1000));
    }

    // --------------- mock data out -----------------

    /*    ------  REAL CODE -----------

    console.log('[AuthService] Conectando a API LOGIN:', this.apiUrl);

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        // Solo guardamos sesión si el backend dice success: true
        if (response.success && response.user && response.token) {
          console.log('Login real exitoso');
          this.setSession(response.user, response.token);
        }
      })
    );

*/

  }

  logout(): void {
    console.log('[AuthService] Cerrando sesión...');
    localStorage.removeItem('chefpro_user');
    localStorage.removeItem('chefpro_token');
    this.currentUserSubject.next(null);
    this.router.navigate(['/index']);
  }

  // private methods
  // save user
  private setSession(user: User, token: string): void {
    localStorage.setItem('chefpro_user', JSON.stringify(user));
    localStorage.setItem('chefpro_token', token);
    this.currentUserSubject.next(user);
  }

  // rescue user
  private restoreSession(): void {
    const userJson = localStorage.getItem('chefpro_user');
    const token = localStorage.getItem('chefpro_token');

    if (userJson && token) {
      try {
        const user: User = JSON.parse(userJson);
        this.currentUserSubject.next(user);
      } catch (e) {
        this.logout();
      }
    }
  }
}
