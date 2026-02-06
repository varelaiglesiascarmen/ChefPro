import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class ChefService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = `${environment.apiUrl}`;

  // get details about the chef
  getChefDetails(chefId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/chefs/${chefId}`);
  }

  // update chef's menu
  updateChef(chefId: number, chefData: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/chefs/${chefId}`, chefData);
  }

  // obtain the chef's menus
  getChefMenus(chefId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/menu/chef/${chefId}`);
  }

  // delete some menu
  deleteMenu(menuId: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/menu/${menuId}`);
  }

  // obtain reservations orders
  getChefReservations(chefId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/reservations/chef/${chefId}`);
  }

  // change reservation status
  updateReservationStatus(chefId: number, date: string, status: string): Observable<any> {
    const payload = { chef_ID: chefId, date: date, status: status };
    return this.http.put<any>(`${this.apiUrl}/reservations/status`, payload);
  }

  // Create menu
  createMenu(menuData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/chef/menus`, menuData);
  }

  // create dish
  createDish(dishData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/chef/plato`, dishData);
  }

  // save allergen
  saveAllergen(allergenData: any) {
    return this.http.post(`${this.apiUrl}/alergenos`, allergenData);
  }

}
