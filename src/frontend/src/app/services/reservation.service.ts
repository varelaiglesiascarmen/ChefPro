import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReservationCreateDto } from '../models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  /**
   * Sends a new reservation request to the backend.
   * POST /api/reservations — the backend identifies the diner via JWT.
   * @param dto Object matching ReservationsCReqDto (chefId, menuId, date, numberOfDiners, address).
   * @returns Observable that completes on 201 Created.
   */
  createReservation(dto: ReservationCreateDto): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/reservations`, dto);
  }
}
