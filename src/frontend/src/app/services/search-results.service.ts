import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { ChefFilter, ChefSearchResponse, DietOption } from '../models/search-results.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChefService {
  private http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  searchChefs(filter: ChefFilter): Observable<ChefSearchResponse> {
    let params = new HttpParams();

    // q puede venir como searchText (desde search-filter.component) o como q (desde URL params)
    const queryText = filter.q || filter.searchText || '';
    if (queryText.trim() !== '') {
      params = params.set('q', queryText.trim());
    }

    if (filter.date) {
      params = params.set('date', filter.date);
    }
    if (filter.minPrice != null) {
      params = params.set('minPrice', filter.minPrice.toString());
    }
    if (filter.maxPrice != null) {
      params = params.set('maxPrice', filter.maxPrice.toString());
    }

    // guests puede venir como guestCount (desde search-filter) o como guests (desde URL)
    const guests = filter.guests ?? filter.guestCount;
    if (guests != null) {
      params = params.set('guests', guests.toString());
    }

    if (filter.allergens && filter.allergens.length > 0) {
      filter.allergens.forEach(a => {
        params = params.append('allergens', a);
      });
    }

    return this.http.get<ChefSearchResponse>(`${this.apiUrl}/chef/search`, { params });
  }

  // Las 4 opciones del panel de filtros mapeadas a sus nombres exactos en official_allergens_list
  getDietOptions(): Observable<DietOption[]> {
    return of([
      { id: 1, label: 'Vegano',      value: 'Vegano' },
      { id: 2, label: 'Sin Gluten',  value: 'Gluten' },
      { id: 3, label: 'Halal',       value: 'Halal' },
      { id: 4, label: 'Keto',        value: 'Keto' }
    ]);
  }
}
