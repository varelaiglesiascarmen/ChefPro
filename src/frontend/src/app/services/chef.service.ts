import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { ChefProfileUpdate, ChefPublicDetail, MenuPublicDetail } from '../models/chef-detail.model';
import { ReservationStatusUpdate } from '../models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ChefService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = `${environment.apiUrl}`;

  // PUBLIC ENDPOINTS (no authentication)

  getChefPublicProfile(chefId: number): Observable<ChefPublicDetail> {
    return this.http.get<ChefPublicDetail>(`${this.apiUrl}/chef/${chefId}/profile`);
  }

  getMenuPublicDetail(menuId: number): Observable<MenuPublicDetail> {
    return this.http.get<MenuPublicDetail>(`${this.apiUrl}/chef/menus/${menuId}/public`);
  }

  // ========================================
  // AUTHENTICATED ENDPOINTS — CHEF PROFILE
  // ========================================

  updateChefProfile(chefData: ChefProfileUpdate): Observable<ChefPublicDetail> {
    return this.http.patch<ChefPublicDetail>(`${this.apiUrl}/chef/profile`, chefData);
  }

  /**
   * Sube la foto de perfil del chef.
   * Recibe un File, construye el FormData y llama a POST /api/chef/profile/photo.
   * Devuelve { photo: "data:image/jpeg;base64,..." }
   */
  uploadChefPhoto(file: File): Observable<{ photo: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ photo: string }>(`${this.apiUrl}/chef/profile/photo`, formData);
  }

  /**
   * Sube la foto de portada del chef.
   * Recibe un File, construye el FormData y llama a POST /api/chef/profile/cover-photo.
   * Devuelve { coverPhoto: "data:image/jpeg;base64,..." }
   */
  uploadChefCoverPhoto(file: File): Observable<{ coverPhoto: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ coverPhoto: string }>(`${this.apiUrl}/chef/profile/cover-photo`, formData);
  }


  // AUTHENTICATED ENDPOINTS — MENUS

  createMenu(menuData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/chef/menus`, menuData);
  }

  deleteMenu(menuId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/chef/menus/${menuId}`);
  }


  // AUTHENTICATED ENDPOINTS — DISHES


  createDish(dishData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/chef/plato`, dishData);
  }

  // AUTHENTICATED ENDPOINTS — RESERVATIONS

  getChefReservations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/reservations/chef`);
  }

  updateReservationStatus(payload: ReservationStatusUpdate): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/reservations/status`, payload);
  }
}
