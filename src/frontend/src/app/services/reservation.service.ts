import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/reservations`;

  private getHeaders() {
    const token = localStorage.getItem('chefpro_token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  requestBooking(data: any) {
    return this.http.post(this.url, { ...data, status: 'PENDING' }, { headers: this.getHeaders() });
  }

  getReservations() {
    return this.http.get<any[]>(this.url, { headers: this.getHeaders() });
  }

  updateStatus(id: number, status: 'ACCEPTED' | 'REJECTED') {
    return this.http.patch(`${this.url}/${id}`, { status }, { headers: this.getHeaders() });
  }
}
