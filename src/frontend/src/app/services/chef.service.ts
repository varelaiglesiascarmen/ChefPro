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

  // ========================================
  // PUBLIC ENDPOINTS (no authentication)
  // ========================================

  /** Chef public profile */
  getChefPublicProfile(chefId: number): Observable<ChefPublicDetail> {
    return this.http.get<ChefPublicDetail>(`${this.apiUrl}/chef/${chefId}/profile`);
  }

  /** Public menu detail */
  getMenuPublicDetail(menuId: number): Observable<MenuPublicDetail> {
    return this.http.get<MenuPublicDetail>(`${this.apiUrl}/chef/menus/${menuId}/public`);
  }

  // ========================================
  // AUTHENTICATED ENDPOINTS — CHEF PROFILE
  // ========================================

  /**
   * Partially updates the authenticated chef's profile.
   * PATCH /api/chef/profile — the backend identifies the chef via JWT.
   */
  updateChefProfile(chefData: ChefProfileUpdate): Observable<ChefPublicDetail> {
    return this.http.patch<ChefPublicDetail>(`${this.apiUrl}/chef/profile`, chefData);
  }

  // ========================================
  // AUTHENTICATED ENDPOINTS — MENUS
  // ========================================

  /** Creates a new menu for the authenticated chef */
  createMenu(menuData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/chef/menus`, menuData);
  }

  /**
   * Deletes a menu owned by the authenticated chef.
   * DELETE /api/chef/menus/{menuId}
   */
  deleteMenu(menuId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/chef/menus/${menuId}`);
  }

  // ========================================
  // AUTHENTICATED ENDPOINTS — DISHES
  // ========================================

  /** Creates a new dish */
  createDish(dishData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/chef/plato`, dishData);
  }

  // ========================================
  // AUTHENTICATED ENDPOINTS — RESERVATIONS
  // ========================================

  /**
   * Retrieves the authenticated chef's reservations.
   * GET /api/reservations/chef — the backend identifies the chef via JWT.
   */
  getChefReservations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/reservations/chef`);
  }

  /**
   * Updates the status of a reservation.
   * PATCH /api/reservations/status — the backend expects PATCH, not PUT.
   */
  updateReservationStatus(payload: ReservationStatusUpdate): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/reservations/status`, payload);
  }
}
