import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, delay } from 'rxjs';
import { Chef, DietOption, ChefFilter, Page } from '../models/search-results.model';
import { environment } from '../../environments/environment';

/* ------------------- mock data ------------------- */
const MOCK_CHEFS: Chef[] = [
  {
    id: 101,
    name: 'Chef Mario Rossi',
    photoUrl: 'https://i.pravatar.cc/300?img=11',
    rating: 4.8,
    pricePerPerson: 45,
    location: 'Madrid',
    specialties: ['Pasta Fresca', 'Risotto', 'Italiana', 'Mediterránea', 'Casera', 'Económico', 'Sabor de Italia']
  },
  {
    id: 102,
    name: 'Chef Laura García',
    photoUrl: 'https://i.pravatar.cc/300?img=5',
    rating: 5.0,
    pricePerPerson: 120,
    location: 'Valencia',
    specialties: ['Paella', 'Marisco', 'Arroz', 'Mediterránea', 'Gourmet', 'Lujo', 'Premium', 'Mar y Montaña']
  },
  {
    id: 103,
    name: 'Chef Kenji Sato',
    photoUrl: 'https://i.pravatar.cc/300?img=15',
    rating: 4.5,
    pricePerPerson: 65,
    location: 'Barcelona',
    specialties: ['Sushi', 'Japonesa', 'Asiática', 'Fusión', 'Pescado', 'Oriental', 'Omakase']
  },
  {
    id: 104,
    name: 'Chef Pierre Dubois',
    photoUrl: 'https://i.pravatar.cc/300?img=3',
    rating: 4.9,
    pricePerPerson: 200,
    location: 'Paris',
    specialties: ['Francesa', 'Carne', 'Vino', 'Romántica', 'Exclusivo', 'Lujo', 'Gourmet', 'Alta Cocina']
  }
];

@Injectable({
  providedIn: 'root'
})
export class ChefService {
  private http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  /**
   * Filters the chef list based on search criteria.
   * Simulates a backend SQL query using 'LIKE' and range operators.
   * @param filter Selection criteria for text and price range.
   * @returns Paginated result set.
   */
  searchChefs(filter: ChefFilter): Observable<Page<Chef>> {
    let results = MOCK_CHEFS;

    // Implementation of multi-field search (Name, Location, and Specialties)
    if (filter.searchText) {
      const term = filter.searchText.toLowerCase().trim();

      results = results.filter(chef => {
        const matchName = chef.name.toLowerCase().includes(term);
        const matchLoc = chef.location.toLowerCase().includes(term);
        const matchTag = chef.specialties.some(tag => tag.toLowerCase().includes(term));

        return matchName || matchLoc || matchTag;
      });
    }

    // Price range filtering logic
    if (filter.minPrice) {
      results = results.filter(c => c.pricePerPerson >= filter.minPrice!);
    }
    if (filter.maxPrice) {
      results = results.filter(c => c.pricePerPerson <= filter.maxPrice!);
    }

    // Fallback mechanism: Provides featured suggestions if no matches are found
    if (results.length === 0) {
      results = [MOCK_CHEFS[0], MOCK_CHEFS[2]];
    }

    // Mapping result set to a standard Spring Boot Pageable response structure
    const mockPage: Page<Chef> = {
      content: results,
      totalElements: results.length,
      totalPages: 1,
      size: 10,
      number: 0
    };

    return of(mockPage).pipe(delay(500));
  }

  /**
   * Persists a new reservation record to the database.
   * @param bookingData Object containing chef_ID, menu_ID, n_diners, and date.
   */
  /** @deprecated Use ReservationService.createReservation() instead. */
  createReservation(bookingData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/reservations`, bookingData);
  }

  /**
   * Retrieves available dietary restrictions from the official allergen registry.
   */
  getDietOptions(): Observable<DietOption[]> {
    return of([
      { id: 1, label: 'Vegano', value: 'VEGAN' },
      { id: 2, label: 'Sin Gluten', value: 'GLUTEN_FREE' },
      { id: 3, label: 'Halal', value: 'HALAL' },
      { id: 4, label: 'Keto', value: 'KETO' }
    ]);
  }
}
