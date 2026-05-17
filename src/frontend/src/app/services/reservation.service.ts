import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { ReservationCreateDto, ReservationStatusUpdate } from '../models/reservation.model';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/reservations`;

  createReservation(data: ReservationCreateDto): Observable<any> {
    return this.http.post(this.url, { ...data, status: 'PENDING' });
  }

  getReservations(): Observable<any[]> {
    return this.http.get<any[]>(this.url);
  }

  getChefReservations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.url}/chef`);
  }

  updateReservationStatus(payload: ReservationStatusUpdate): Observable<any> {
    return this.http.patch(`${this.url}/status`, payload);
  }
}
