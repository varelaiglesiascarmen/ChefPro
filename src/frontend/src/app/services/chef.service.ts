import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Definimos la forma de los datos que esperamos recibir
export interface DietOption {
  id: number;
  label: string;
  value: string;
}

@Injectable({ providedIn: 'root' })
export class ChefService {
  private http = inject(HttpClient);
  // Esta URL debe coincidir con la que configure tu compañera en el backend
  private apiUrl = 'http://localhost:8080/api';

  // 1. Pedir las opciones de dietas al servidor
  getDietOptions(): Observable<DietOption[]> {
    return this.http.get<DietOption[]>(`${this.apiUrl}/config/diets`);
  }

  // 2. Enviar los filtros para buscar chefs
  searchChefs(filters: any): Observable<any[]> {
    // Esto enviará algo como: POST http://localhost:8080/api/chefs/search
    return this.http.post<any[]>(`${this.apiUrl}/chefs/search`, filters);
  }
}
