import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);

  // Ajusta esta URL cuando tu compañera te dé la suya
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor() { }

  login(credenciales: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credenciales).pipe(
      tap((respuesta: any) => {
        // Guardamos el token que nos envíe Spring Boot
        if (respuesta && respuesta.token) {
          localStorage.setItem('token', respuesta.token);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
  }
}
