import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
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
  // DEVELOPER MODE BLOCK (isDevMode)
  // ==========================================
  private isDevMode = false; // Using real backend API

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

  // LOGIN WITH BYPASS
  login(credentials: LoginRequest): Observable<User> {
    if (this.isDevMode) {
      const mock = this.getMockUser();
      localStorage.setItem('chefpro_token', 'dev-token-secret');
      this.setSession(mock);
      return of(mock);
    }

    return this.http.post<any>(`${this.authUrl}/login`, credentials).pipe(
      tap(response => {
        if (response.token) localStorage.setItem('chefpro_token', response.token);
      }),
      switchMap(response => {
        // Map backend response to frontend User model
        const user: User = this.mapBackendUserToFrontend(response.user);
        this.setSession(user);
        return of(user);
      }),
      catchError(this.handleError)
    );
  }

  // SIGNUP WITH BYPASS
  signup(data: any): Observable<User> {
    if (this.isDevMode) {
      const mock = this.getMockUser(data.role || 'CHEF');
      localStorage.setItem('chefpro_token', 'dev-token-secret');
      this.setSession(mock);
      return of(mock);
    }

    return this.http.post<any>(`${this.authUrl}/signup`, data).pipe(
      tap(response => {
        if (response.token) localStorage.setItem('chefpro_token', response.token);
      }),
      switchMap(response => {
        // Map backend response to frontend User model
        // Pass the original signup data to fill in missing fields
        const user: User = this.mapBackendUserToFrontend(response.user, data);
        this.setSession(user);
        return of(user);
      }),
      catchError(this.handleError)
    );
  }

  // GET USER DATA (With protection for Dev mode)
  getUserData(): Observable<User> {
    if (this.isDevMode) return of(this.getMockUser());

    return this.http.get<any>(`${this.authUrl}/me`).pipe(
      switchMap(backendUser => {
        const user: User = this.mapBackendUserToFrontend(backendUser);
        this.currentUserSubject.next(user);
        localStorage.setItem('chefpro_user', JSON.stringify(user));
        return of(user);
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
      // Restore user from localStorage immediately (optimistic)
      if (userJson) {
        try {
          const user = JSON.parse(userJson);
          this.currentUserSubject.next(user);
        } catch (e) {
        }
      }

      // Then validate token with backend (only if not in dev mode)
      if (!this.isDevMode) {
        this.getUserData().subscribe({
          error: () => {
            this.logout();
          }
        });
      }
    }
  }

  private handleError(error: HttpErrorResponse) {
    return throwError(() => error);
  }

  // Map backend user response to frontend User model
  private mapBackendUserToFrontend(backendUser: any, additionalData?: any): User {
    let cleanRole = backendUser.role;
    if (cleanRole && cleanRole.startsWith('ROLE_')) {
      cleanRole = cleanRole.replace('ROLE_', '');
    }

    const mappedUser: User = {
      user_ID: backendUser.id,
      userName: additionalData?.username || backendUser.username || backendUser.email?.split('@')[0] || 'user',
      name: backendUser.name || '',
      lastname: additionalData?.surname || backendUser.surname || backendUser.lastname || '',
      email: backendUser.email,
      phone_number: additionalData?.phoneNumber || backendUser.phoneNumber || backendUser.phone_number,
      role: cleanRole,
      reviews_count: backendUser.reviews_count || 0,
      rating_avg: backendUser.rating_avg || 0,
      languages: backendUser.languages || [],
      photoUrl: backendUser.photoUrl || backendUser.photo,
      address: backendUser.address,
      bio: backendUser.bio,
      prizes: backendUser.prizes
    };

    return mappedUser;
  }

  // Update user profile
  updateUser(updatedUser: User): Observable<User> {
    const token = localStorage.getItem('chefpro_token');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    // Map frontend User to backend UpdateProfileDto
    const updateDto = {
      name: updatedUser.name,
      surname: updatedUser.lastname,
      username: updatedUser.userName,
      photo: updatedUser.photoUrl,
      bio: updatedUser.bio,
      prizes: updatedUser.prizes,
      address: updatedUser.address
    };

    return this.http.put<any>(`${this.authUrl}/profile`, updateDto, { headers }).pipe(
      switchMap(backendUser => {
        // Map response back to frontend User model
        const user: User = this.mapBackendUserToFrontend(backendUser);
        this.setSession(user);
        return of(user);
      }),
      catchError(this.handleError)
    );
  }

  // Delete user account permanently
  deleteAccount(): Observable<void> {
    const token = localStorage.getItem('chefpro_token');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    return this.http.delete<void>(`${this.authUrl}/account`, { headers }).pipe(
      tap(() => {
        localStorage.removeItem('chefpro_user');
        localStorage.removeItem('chefpro_token');
        this.currentUserSubject.next(null);
      }),
      catchError(this.handleError)
    );
  }
}
