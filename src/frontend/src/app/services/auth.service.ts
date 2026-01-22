import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

// 1. Interfaz para el usuario (necesaria para el Navbar)
export interface User {
  id: string;
  name: string;
  photoURL?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth'; // Tu URL real

  // 2. Propiedad para gestionar el usuario actual (necesaria para la imagen)
  // Lo inicializamos con datos FALSOS para que veas la imagen mientras no tengas Backend.
  // Cuando tengas backend, esto debería empezar en 'null'.
  currentUser: User | null = {
    id: '1',
    name: 'Chef Pro',
    photoURL: 'https://i.pravatar.cc/150?img=68'
  };

  constructor() {
    // Opcional: Al cargar la app, podrías comprobar si hay token
    // y recuperar el usuario real. Por ahora lo dejamos simulado.
  }

  // --- LÓGICA DE CONEXIÓN (Tu código original mejorado) ---

  login(credenciales: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credenciales).pipe(
      tap((respuesta: any) => {
        if (respuesta && respuesta.token) {
          // Guardamos el token
          localStorage.setItem('token', respuesta.token);

          // AQUÍ DEBERÍAMOS ACTUALIZAR EL USUARIO
          // Como tu backend aun no devuelve los datos del usuario (nombre, foto),
          // simularemos que al loguearse se rellenan los datos.
          this.currentUser = {
            id: '1',
            name: credenciales.username || 'Usuario',
            photoURL: 'https://i.pravatar.cc/150?img=68'
          };
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    this.currentUser = null; // Borramos el usuario de la memoria para que se quite la foto
  }

  // --- LÓGICA DE ESTADO ---

  isLoggedIn(): boolean {
    // Verificamos si hay token O si tenemos usuario en memoria
    return !!localStorage.getItem('token') || this.currentUser !== null;
  }

  // Método auxiliar para obtener el token si lo necesitas en interceptores
  getToken(): string | null {
    return localStorage.getItem('token');
  }
}
