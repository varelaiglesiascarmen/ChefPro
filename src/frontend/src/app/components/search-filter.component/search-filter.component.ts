import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

// 1. Definimos la estructura de una opción de dieta
export interface DietOption {
  label: string;    // Lo que ve el usuario (Español) -> 'Vegano'
  value: string;    // Lo que enviamos al backend (Inglés) -> 'vegan'
  selected: boolean; // Estado del checkbox
}

// 2. Definimos el objeto del Estado del Filtro
export interface FilterState {
  diets: DietOption[];
  minPrice: number | null;
  maxPrice: number | null;
  guestCount: number | null;
  onlyTopRated: boolean;
}

@Component({
  selector: 'app-search-filter',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './search-filter.component.html',
  styleUrl: './search-filter.component.css'
})
export class SearchFilterComponent {

  @Output() close = new EventEmitter<void>();
  @Output() apply = new EventEmitter<FilterState>();

  // Opciones de configuración (Hardcoded o desde API)
  dietOptions: DietOption[] = [
    { label: 'Vegano', value: 'vegan', selected: false },
    { label: 'Sin Gluten', value: 'gluten_free', selected: false },
    { label: 'Vegetariano', value: 'vegetarian', selected: false },
    { label: 'Keto', value: 'keto', selected: false },
    { label: 'Halal', value: 'halal', selected: false },
    { label: 'Kosher', value: 'kosher', selected: false },
    { label: 'Paleo', value: 'paleo', selected: false },
    { label: 'DASH', value: 'dash', selected: false },
    { label: 'Mediterráneo', value: 'mediterranean', selected: false },
    { label: 'Sin Lactosa', value: 'lactose_free', selected: false },
    { label: 'Sin Frutos Secos', value: 'nut_free', selected: false },
    { label: 'Sin Huevo', value: 'egg_free', selected: false },
    { label: 'Sin Leche', value: 'dairy_free', selected: false },
    { label: 'Sin Pescado', value: 'fish_free', selected: false },
    { label: 'Sin Mariscos', value: 'shellfish_free', selected: false },
    { label: 'Sin Soja', value: 'soy_free', selected: false },
    { label: 'Sin Trigo', value: 'wheat_free', selected: false },
    { label: 'Sin Azúcar', value: 'sugar_free', selected: false },
    { label: 'Bajo en Carbohidratos', value: 'low_carb', selected: false },
    { label: 'Bajo en Grasas', value: 'low_fat', selected: false },
    { label: 'Alto en Proteínas', value: 'high_protein', selected: false },
  ];

  // Estado actual de los filtros
  filterState: FilterState = {
    diets: this.dietOptions, // Referencia al array de arriba
    minPrice: null,
    maxPrice: null,
    guestCount: null,
    onlyTopRated: false
  };

  // Método para limpiar/resetear formulario
  resetFilters() {
    // 1. Resetear checkboxes
    this.dietOptions.forEach(option => option.selected = false);

    // 2. Resetear valores numéricos
    this.filterState.minPrice = null;
    this.filterState.maxPrice = null;
    this.filterState.guestCount = null;
    this.filterState.onlyTopRated = false;
  }

  // Método para aplicar y enviar datos al padre
  applyFilters() {
    // Enviamos una copia del estado para evitar mutaciones externas
    this.apply.emit({ ...this.filterState });
  }
}
