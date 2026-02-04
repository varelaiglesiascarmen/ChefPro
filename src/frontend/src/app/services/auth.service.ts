import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { LoginRequest, LoginResponse, User, RegisterRequest } from '../models/auth.model';
import { environment } from '../../environments/environment';

// Injectable makes it possible to call the service from any component
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  // environment URL > (http://localhost:8081/api) + /auth
  private apiUrl = `${environment.apiUrl}/auth`;

  // STATE MANAGEMENT
  // BehaviorSubject > Save the current user
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  // user$ > components (such as the Navbar) “subscribe” to this
  public user$ = this.currentUserSubject.asObservable();

  constructor() {
    this.restoreSession();
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  // login > Receives the credentials and returns an Observable with the response from the backend
  login(credentials: LoginRequest): Observable<LoginResponse> {
    console.log('[AuthService] Conectando a:', `${this.apiUrl}/login`);

    /*

    http.post > Sends the username and password to the backend.

    pipe() > This is a pipe; the response from the backend passes through here before reaching the component.

    tap() > Used to intercept success. If the login is correct, we take the opportunity to save the data in the
    browser (localStorage) before the component finds out.

    */

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (response.token) {
          console.log('Login exitoso, guardando sesión...');
          this.setSession(
            /*
            Save the user who sends back in response.user. If back fails, this saves us by using
            decodeUserFromToken to detect what type of user it is.
            */
            response.user || this.decodeUserFromToken(credentials.username, response.token),
            response.token
          );
        }
      }),
      catchError(this.handleError)
    );
  }

  // sign in > Registers a new user
  register(data: RegisterRequest): Observable<any> {
    console.log('[AuthService] Registrando usuario en:', `${this.apiUrl}/register`);
    return this.http.post(`${this.apiUrl}/register`, data).pipe(
      tap(() => console.log('Registro real completado')),
      catchError(this.handleError)
    );
  }

  // real-time validation > Check if username or email is already taken
  checkUsernameAvailability(username: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/check-username/${username}`).pipe(
      catchError(() => of(true))
    );
  }

  // real-time validation > Check if email is already taken
  checkEmailAvailability(email: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/check-email/${email}`).pipe(
      catchError(() => of(true))
    );
  }

  /**
   catchError(() => of(true))

    Meaning: “If I try to ask the backend if the user exists and the backend gives
    me an error (or does not have that endpoint), I will assume that the user is free.”
   */

  // logout > Clear user session
  logout(): void {
    console.log('[AuthService] Cerrando sesión...');
    localStorage.removeItem('chefpro_user');
    localStorage.removeItem('chefpro_token');
    this.currentUserSubject.next(null);
    this.router.navigate(['/index']);
  }

  // remember user session
  private setSession(user: User, token: string): void {
    localStorage.setItem('chefpro_user', JSON.stringify(user));
    localStorage.setItem('chefpro_token', token);
    this.currentUserSubject.next(user);
  }

  // restore user session
  private restoreSession(): void {
    const userJson = localStorage.getItem('chefpro_user');
    const token = localStorage.getItem('chefpro_token');
    if (userJson && token) {
      try {
        const user: User = JSON.parse(userJson);
        this.currentUserSubject.next(user);
      } catch (e) {
        console.error('Error restaurando sesión', e);
        this.logout();
      }
    }
  }

  // error handling
  private handleError(error: HttpErrorResponse) {
    console.error('Error en AuthService:', error);
    return throwError(() => error);
  }

  // decode JWT token to extract user role
  private decodeUserFromToken(username: string, token: string): User {
    let role: 'DINER' | 'CHEF' | 'ADMIN' = 'DINER';

    try {
      const payloadPart = token.split('.')[1];
      const payloadDecoded = atob(payloadPart);
      const values = JSON.parse(payloadDecoded);

      // search for response from the back
      const tokenRole = values.role || values.roles || values.authority;

      // allocation logic
      if (tokenRole === 'CHEF') role = 'CHEF';
      if (tokenRole === 'ADMIN') role = 'ADMIN';

    } catch (e) {
      console.warn('No se pudo leer el rol del token, asignando DINER por defecto');
    }

    return {
      id: 'temp-id',
      username: username,
      name: username,
      email: 'temp@email.com',
      role: role, 
      photoUrl: ''
    };
  }
}

