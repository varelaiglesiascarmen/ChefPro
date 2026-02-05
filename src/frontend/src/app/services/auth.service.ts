import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap, switchMap } from 'rxjs/operators';
import { LoginRequest, LoginResponse, User } from '../models/auth.model';
import { environment } from '../../environments/environment';
import { of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  // URL base: http://localhost:8081/api/auth
  private authUrl = `${environment.apiUrl}/auth`;

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public user$ = this.currentUserSubject.asObservable();

  constructor() {
    this.restoreSession();
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  // LOGIN
  login(credentials: LoginRequest): Observable<User> {
    return this.http.post<any>(`${this.authUrl}/login`, credentials).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('chefpro_token', response.token);
        }
      }),
      switchMap(() => this.getUserData()),
      tap(fullUser => this.setSession(fullUser)),
      catchError(this.handleError)
    );
  }

  // SIGNUP
  signup(data: any): Observable<User> {
    return this.http.post<any>(`${this.authUrl}/signup`, data).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('chefpro_token', response.token);
        }
      }),
      switchMap(() => this.getUserData()),
      tap(fullUser => this.setSession(fullUser)),
      catchError(this.handleError)
    );
  }

  // GET USER DATA
  getUserData(): Observable<User> {
    return this.http.get<User>(`${this.authUrl}/me`).pipe(
      tap(user => {
        this.currentUserSubject.next(user);
        localStorage.setItem('chefpro_user', JSON.stringify(user));
      }),
      catchError(this.handleError)
    );
  }

  // CHECK USERNAME & EMAIL AVAILABILITY
  checkUsernameAvailability(username: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.authUrl}/check-username?username=${username}`);
  }

  checkEmailAvailability(email: string): Observable<boolean> {
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
    if (token) {
      this.getUserData().subscribe({
        error: () => this.logout()
      });
    }
  }

  private handleError(error: HttpErrorResponse) {
    console.error('Error en el proceso de autenticación:', error);
    return throwError(() => error);
  }

}
