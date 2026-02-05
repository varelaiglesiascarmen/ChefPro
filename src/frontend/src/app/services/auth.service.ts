import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError, of } from 'rxjs';
import { catchError, tap, switchMap } from 'rxjs/operators';
import { LoginRequest, LoginResponse, User, signupRequest } from '../models/auth.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = `${environment.apiUrl}/auth`;

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public user$ = this.currentUserSubject.asObservable();

  constructor() {
    this.restoreSession();
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  login(credentials: LoginRequest): Observable<User> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('chefpro_token', response.token);
        }
      }),
      switchMap(() => this.getUserData()),
      tap(user => {
        this.setSession(user);
      }),
      catchError(this.handleError)
    );
  }

  signup(data: signupRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/signup`, data).pipe(
      tap(() => console.log('Registro en /signup exitoso')),
      catchError(this.handleError)
    );
  }

  getUserData(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`).pipe(
      tap(user => this.currentUserSubject.next(user)),
      catchError(this.handleError)
    );
  }

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

  private restoreSession(): void {
    const userJson = localStorage.getItem('chefpro_user');
    const token = localStorage.getItem('chefpro_token');
    if (userJson && token) {
      this.currentUserSubject.next(JSON.parse(userJson));
    }
  }

  private handleError(error: HttpErrorResponse) {
    return throwError(() => error);
  }
}
