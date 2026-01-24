import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Chef, DietOption, ChefFilter, Page } from '../models/chef.model';

@Injectable({
  providedIn: 'root'
})
export class ChefService {
  private http = inject(HttpClient);

  // URL base > I must replace it with the real one when I do it back
  private apiUrl = 'http://localhost:8080/api';

  getDietOptions(): Observable<DietOption[]> {
    return this.http.get<DietOption[]>(`${this.apiUrl}/config/diets`);
  }

  searchChefs(filter: ChefFilter): Observable<Page<Chef>> {
    const payload = {
      ...filter,
      page: filter.page ?? 0,
      size: filter.size ?? 10,
      sort: filter.sort ?? 'price,asc'
    };

    return this.http.post<Page<Chef>>(`${this.apiUrl}/chefs/search`, payload);
  }
}
