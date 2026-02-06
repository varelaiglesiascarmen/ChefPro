import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/menus`;

  private getHeaders() {
    const token = localStorage.getItem('chefpro_token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  getMenusByChef(chefId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/chef/${chefId}`, { headers: this.getHeaders() });
  }

  createMenu(menuData: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, menuData, { headers: this.getHeaders() });
  }

  deleteMenu(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }
}
