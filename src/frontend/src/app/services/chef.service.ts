import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { ChefPublicDetail, MenuPublicDetail } from '../models/chef-detail.model';

@Injectable({
  providedIn: 'root'
})
export class ChefService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = `${environment.apiUrl}`;

  // Perfil público del chef (sin autenticación)
  getChefPublicProfile(chefId: number): Observable<ChefPublicDetail> {
    return this.http.get<ChefPublicDetail>(`${this.apiUrl}/chef/${chefId}/profile`);
  }

  // Detalle público de un menú (sin autenticación)
  getMenuPublicDetail(menuId: number): Observable<MenuPublicDetail> {
    return this.http.get<MenuPublicDetail>(`${this.apiUrl}/chef/menus/${menuId}/public`);
  }

  // update chef's menu
  updateChef(chefId: number, chefData: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/chefs/${chefId}`, chefData);
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
