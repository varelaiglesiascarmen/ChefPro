// Generic interface for Spring Boot PAGINATION
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Chef Model (What the list returns)
export interface Chef {
  id: number;
  name: string;
  photoUrl?: string;
  rating: number;
  pricePerPerson: number;
  location: string;
  specialties: string[];
}

// Diet Options (For the checkboxes)
export interface DietOption {
  id: number;
  label: string;
  value: string; // "VEGAN", "KETO"...
  selected?: boolean; // Solo para el front
}

// Filter we send to the Backend (POST Payload)
export interface ChefFilter {
  searchText?: string;
  dietIds?: number[];
  minPrice?: number | null;
  maxPrice?: number | null;
  guestCount?: number | null;
  onlyTopRated?: boolean;

  // Pagination
  page?: number;
  size?: number;
  sort?: string;
}
