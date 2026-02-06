import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap, switchMap } from 'rxjs/operators';
import { LoginRequest, LoginResponse, User } from '../models/auth.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private authUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public user$ = this.currentUserSubject.asObservable();

  // ==========================================
  // BLOQUE MODO DESARROLLADOR (isDevMode)
  // ==========================================
  private isDevMode = true; // CAMBIA A 'false' PARA CONECTAR CON EL BACKEND REAL

  private getMockUser(role: 'CHEF' | 'DINER' = 'CHEF'): User {
    return {
      user_ID: 1,
      userName: 'yolo',
      role: role,
      name: 'Chef',
      lastname: 'Pro (Modo Dev)',
      email: 'chef@pro.com',
      rating_avg: 4.9,
      reviews_count: 124,
      languages: ['Español', 'Inglés'],
      photoUrl: 'https://images.unsplash.com/photo-1577219491135-ce391730fb2c'
    };
  }
  // ==========================================

  constructor() {
    this.restoreSession();
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  // LOGIN CON BYPASS
  login(credentials: LoginRequest): Observable<User> {
    if (this.isDevMode) {
      console.warn('⚠️ MODO DEV: Saltando login real');
      const mock = this.getMockUser();
      localStorage.setItem('chefpro_token', 'dev-token-secret');
      this.setSession(mock);
      return of(mock);
    }

    return this.http.post<any>(`${this.authUrl}/login`, credentials).pipe(
      tap(response => {
        if (response.token) localStorage.setItem('chefpro_token', response.token);
      }),
      switchMap(() => this.getUserData()),
      tap(fullUser => this.setSession(fullUser)),
      catchError(this.handleError)
    );
  }

  // SIGNUP CON BYPASS
  signup(data: any): Observable<User> {
    if (this.isDevMode) {
      console.warn('MODO DEV: Saltando registro real');
      const mock = this.getMockUser(data.role || 'CHEF');
      localStorage.setItem('chefpro_token', 'dev-token-secret');
      this.setSession(mock);
      return of(mock);
    }

    return this.http.post<any>(`${this.authUrl}/signup`, data).pipe(
      tap(response => {
        if (response.token) localStorage.setItem('chefpro_token', response.token);
      }),
      switchMap(() => this.getUserData()),
      tap(fullUser => this.setSession(fullUser)),
      catchError(this.handleError)
    );
  }

  // GET USER DATA (Con protección para modo Dev)
  getUserData(): Observable<User> {
    if (this.isDevMode) return of(this.getMockUser());

    return this.http.get<User>(`${this.authUrl}/me`).pipe(
      tap(user => {
        this.currentUserSubject.next(user);
        localStorage.setItem('chefpro_user', JSON.stringify(user));
      }),
      catchError(this.handleError)
    );
  }

  checkUsernameAvailability(username: string): Observable<boolean> {
    if (this.isDevMode) return of(true);
    return this.http.get<boolean>(`${this.authUrl}/check-username?username=${username}`);
  }

  checkEmailAvailability(email: string): Observable<boolean> {
    if (this.isDevMode) return of(true);
    return this.http.get<boolean>(`${this.authUrl}/check-email?email=${email}`);
  }

  // LOGOUT
  logout(): void {
    localStorage.removeItem('chefpro_user');
    localStorage.removeItem('chefpro_token');
    this.currentUserSubject.next(null);
    this.router.navigate(['/index']);
  }

  private setSession(user: User): void {
    localStorage.setItem('chefpro_user', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  // RESTORE SESSION
  private restoreSession(): void {
    const token = localStorage.getItem('chefpro_token');
    const userJson = localStorage.getItem('chefpro_user');

    if (token) {
      if (this.isDevMode && userJson) {
        this.currentUserSubject.next(JSON.parse(userJson));
      } else {
        this.getUserData().subscribe({
          error: () => this.logout()
        });
      }
    }
  }

  private handleError(error: HttpErrorResponse) {
    console.error('Error en el proceso de autenticación:', error);
    return throwError(() => error);
  }
}
