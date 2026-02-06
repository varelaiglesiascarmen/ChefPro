import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChefService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  // Create menu
  createMenu(menuData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/chef/menus`, menuData);
  }

  // create dish
  createDish(dishData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/chef/plato`, dishData);
  }

  saveAllergen(allergenData: any) {
    return this.http.post(`${this.apiUrl}/alergenos`, allergenData);
  }

}
