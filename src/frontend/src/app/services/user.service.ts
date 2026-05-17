import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { PublicProfile } from '../interfaces/profile.interface';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}`;

  getUserPublicProfile(userId: number): Observable<PublicProfile> {
    return this.http.get<PublicProfile>(`${this.apiUrl}/public-profile/user/${userId}`);
  }
}
