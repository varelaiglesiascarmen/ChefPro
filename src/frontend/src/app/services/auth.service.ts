import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';

// user interface
export interface User {
  id: string;
  name: string;
  email: string;
  photoURL?: string;
  role?: string;
}

// login response according to Contract 2 of APIs_agreements_ChefPro
interface LoginResponse {
  token: string;
  user: User;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth';

  // user status in memory
  currentUser: User | null = null;

  constructor() {
    // if the token exits, retrieve the user
    if (this.getToken()) {
      this.retrieveUser().subscribe();
    }
  }

  // --- ENDPOINT 1: API ---
  login(credentials: { username: string; password: string }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem('token', response.token);

        this.currentUser = response.user;
      })
    );
  }

  // --- ENDPOINT 2: API ---
  retrieveUser(): Observable<User | null> {
    const token = this.getToken();
    if (!token) return of(null);

    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    return this.http.get<User>(`${this.apiUrl}/me`, { headers }).pipe(
      tap((user) => {
        console.log('Usuario recuperado: ', user);
        this.currentUser = user;
      }),
      catchError((err) => {
        console.error('Error recuperando usuario (token caducado?)', err);
        this.logout();
        return of(null);
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    this.currentUser = null;
  }

  isLoggedIn(): boolean {
    return !!this.currentUser || !!localStorage.getItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
}
