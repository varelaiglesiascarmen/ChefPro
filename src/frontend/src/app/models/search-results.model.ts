// Resultado de búsqueda de chef (viene en chefs[])
export interface ChefSearchResult {
  id: number;
  name: string;
  lastname: string;
  username: string;
  photo?: string;
  bio?: string;
  location?: string;
  avgScore?: number;
  reviewsCount?: number;
  startingPrice?: number;
}

// Resultado de búsqueda de menú (viene en menus[])
export interface MenuSearchResult {
  menuId: number;
  menuTitle: string;
  menuDescription?: string;
  pricePerPerson: number;
  minDiners?: number;
  maxDiners?: number;
  chefId: number;
  chefName: string;
  chefLastname: string;
  chefPhoto?: string;
  chefLocation?: string;
  avgScore?: number;
  reviewsCount?: number;
}

// Full response from /api/chef/search
export interface ChefSearchResponse {
  cities: string[];
  chefs: ChefSearchResult[];
  menus: MenuSearchResult[];
  noResults: boolean; // true when menus are random suggestions due to no matches
}

// Filtros
export interface ChefFilter {
  searchText?: string;
  dietIds?: number[];
  guestCount?: number | null;
  onlyTopRated?: boolean;
  q?: string;
  date?: string | null;
  minPrice?: number | null;
  maxPrice?: number | null;
  guests?: number | null;
  allergens?: string[];
  page?: number;
  size?: number;
  sort?: string;
}

// Diet Options
export interface DietOption {
  id: number;
  label: string;
  value: string;
  selected?: boolean;
}
