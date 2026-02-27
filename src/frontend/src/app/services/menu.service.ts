import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/chef/menus`;
  private dishApiUrl = `${environment.apiUrl}/chef/plato`;

  getMenusByChef(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  createMenu(menuData: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, menuData);
  }

  updateMenu(menuData: any): Observable<any> {
    return this.http.patch<any>(this.apiUrl, menuData);
  }

  createDish(dishData: any): Observable<any> {
    return this.http.post<any>(this.dishApiUrl, dishData);
  }

  updateDish(dishData: any): Observable<any> {
    return this.http.patch<any>(this.dishApiUrl, dishData);
  }

  deleteDish(menuId: number, dishId: number): Observable<any> {
    return this.http.delete<any>(this.dishApiUrl, {
      params: { menuId, dishId }
    });
  }

  deleteMenu(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
