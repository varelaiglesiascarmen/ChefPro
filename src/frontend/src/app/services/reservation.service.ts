import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { ReservationCreateDto, ReservationStatusUpdate } from '../models/reservation.model';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/reservations`;

  private getHeaders() {
    const token = localStorage.getItem('chefpro_token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  createReservation(data: ReservationCreateDto): Observable<any> {
    return this.http.post(this.url, { ...data, status: 'PENDING' }, { headers: this.getHeaders() });
  }

  getReservations(): Observable<any[]> {
    return this.http.get<any[]>(this.url, { headers: this.getHeaders() });
  }

  getChefReservations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.url}/chef`, { headers: this.getHeaders() });
  }

  updateStatus(id: number, status: 'ACCEPTED' | 'REJECTED'): Observable<any> {
    return this.http.patch(`${this.url}/${id}`, { status }, { headers: this.getHeaders() });
  }

  updateReservationStatus(payload: ReservationStatusUpdate): Observable<any> {
    return this.http.patch(`${this.url}/status`, payload, { headers: this.getHeaders() });
  }
}
