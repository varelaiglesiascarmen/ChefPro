import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/chef/menus`;
  private dishApiUrl = `${environment.apiUrl}/chef/plato`;

  private getHeaders() {
    const token = localStorage.getItem('chefpro_token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  getMenusByChef(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  createMenu(menuData: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, menuData, { headers: this.getHeaders() });
  }

  updateMenu(menuData: any): Observable<any> {
    return this.http.patch<any>(this.apiUrl, menuData, { headers: this.getHeaders() });
  }

  createDish(dishData: any): Observable<any> {
    return this.http.post<any>(this.dishApiUrl, dishData, { headers: this.getHeaders() });
  }

  updateDish(dishData: any): Observable<any> {
    return this.http.patch<any>(this.dishApiUrl, dishData, { headers: this.getHeaders() });
  }

  deleteDish(menuId: number, dishId: number): Observable<any> {
    return this.http.delete<any>(this.dishApiUrl, {
      headers: this.getHeaders(),
      params: { menuId, dishId }
    });
  }

  deleteMenu(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }
}
