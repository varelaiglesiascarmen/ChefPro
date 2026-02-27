import { Component, EventEmitter, Output, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ChefService } from '../../services/search-results.service';
import { ChefFilter, DietOption } from '../../models/search-results.model';

/**
 * Component responsible for displaying and managing search filters
 * including dietary preferences, price range (inputs & dual slider),
 * guest count, and rating toggle.
 */
@Component({
  selector: 'app-search-filter',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './search-filter.component.html',
  styleUrl: './search-filter.component.css'
})
export class SearchFilterComponent implements OnInit {

  // Dependencies
  private chefService = inject(ChefService);

  // Outputs
  @Output() close = new EventEmitter<void>();
  @Output() apply = new EventEmitter<ChefFilter>();

  // Data > List of dietary options fetched from the service
  dietOptions: DietOption[] = [];

  // Form State
  searchText: string = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;
  guestCount: number | null = null;
  onlyTopRated: boolean = false;
  selectedDate: string | null = null;
  minDate: string = '';

  // Slider Configuration
  sliderMin: number = 0;
  sliderMax: number = 200;
  sliderStep: number = 5;
  minGap: number = 10;

  ngOnInit() {
    this.loadDiets();
    this.calculateMinDate();
  }

  loadDiets() {
    this.chefService.getDietOptions().subscribe({
      next: (data) => {
        this.dietOptions = data.map(d => ({ ...d, selected: false }));
      },
      error: () => { }
    });
  }

  calculateMinDate() {
    const today = new Date();
    const year = today.getFullYear();
    const month = (today.getMonth() + 1).toString().padStart(2, '0');
    const day = today.getDate().toString().padStart(2, '0');
    this.minDate = `${year}-${month}-${day}`;
  }

  resetFilters() {
    this.dietOptions.forEach(d => d.selected = false);
    this.minPrice = null;
    this.maxPrice = null;
    this.guestCount = null;
    this.onlyTopRated = false;
    this.selectedDate = null;
    this.searchText = '';
  }

  applyFilters() {
    // Convertir las opciones seleccionadas a sus valores (nombres de alérgenos)
    const selectedAllergens = this.dietOptions
      .filter(d => d.selected)
      .map(d => d.value);

    const filters: ChefFilter = {
      searchText: this.searchText,
      minPrice: this.minPrice,
      maxPrice: this.maxPrice,
      guestCount: this.guestCount,
      onlyTopRated: this.onlyTopRated,
      date: this.selectedDate,
      allergens: selectedAllergens
    };

    this.apply.emit(filters);
  }

  // Getters/Setters
  get currentMin(): number { return this.minPrice ?? 0; }
  set currentMin(val: number) { this.minPrice = val; }
  get currentMax(): number { return this.maxPrice ?? this.sliderMax; }
  set currentMax(val: number) { this.maxPrice = val; }

  getLeftPercent(): string {
    const min = this.minPrice ?? 0;
    return ((min / this.sliderMax) * 100) + '%';
  }

  getWidthPercent(): string {
    const min = this.minPrice ?? 0;
    let max = this.maxPrice ?? this.sliderMax;
    if (max > this.sliderMax) max = this.sliderMax;
    return (((max - min) / this.sliderMax) * 100) + '%';
  }

  preventInvalidChars(event: KeyboardEvent): void {
    const invalidChars = ['-', '+', 'e', '.', ','];
    if (invalidChars.includes(event.key)) {
      event.preventDefault();
    }
  }

  validateInput(isMin: boolean) {
    let value = isMin ? this.minPrice : this.maxPrice;
    if (value === null || value === undefined) return;
    if (value < 0) value = 0;
    value = Math.round(value);
    if (isMin) this.minPrice = value;
    else this.maxPrice = value;
    this.validateRange(isMin);
  }

  validateRange(isMin: boolean) {
    let min = this.minPrice ?? 0;
    let max = this.maxPrice ?? this.sliderMax;
    if (isMin) {
      if (min > max - this.minGap) {
        this.minPrice = max - this.minGap;
        if (this.minPrice < 0) this.minPrice = 0;
      }
    } else {
      if (max < min + this.minGap) {
        this.maxPrice = min + this.minGap;
      }
    }
  }
}
