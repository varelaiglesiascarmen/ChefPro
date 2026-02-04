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

  // Lifecycle hook to initialize component data.
  ngOnInit() {
    this.loadDiets();
    this.calculateMinDate();
  }

  // Fetches dietary options from the backend and initializes selection state.
  loadDiets() {
    this.chefService.getDietOptions().subscribe({
      next: (data) => {
        this.dietOptions = data.map(d => ({ ...d, selected: false }));
      },
      error: (err) => console.error('Error loading diets:', err)
    });
  }

  // Calculates today's date in YYYY-MM-DD format for the date picker min attribute.
  calculateMinDate() {
    const today = new Date();
    const year = today.getFullYear();
    const month = (today.getMonth() + 1).toString().padStart(2, '0');
    const day = today.getDate().toString().padStart(2, '0');

    this.minDate = `${year}-${month}-${day}`;
  }

  // Resets all filter fields to their default values.
  resetFilters() {
    this.dietOptions.forEach(d => d.selected = false);
    this.minPrice = null;
    this.maxPrice = null;
    this.guestCount = null;
    this.onlyTopRated = false;
    this.selectedDate = null;
  }

  /*
   Collects all current filter states, constructs the filter object,
   and emits it to the parent component.
   */
  applyFilters() {
    const selectedDietIds = this.dietOptions
      .filter(d => d.selected)
      .map(d => d.id);

    const filters: ChefFilter = {
      dietIds: selectedDietIds,
      minPrice: this.minPrice,
      maxPrice: this.maxPrice,
      guestCount: this.guestCount,
      onlyTopRated: this.onlyTopRated,
      date: this.selectedDate
    };

    this.apply.emit(filters);
  }

  // Getters/Setters
  get currentMin(): number { return this.minPrice ?? 0; }
  set currentMin(val: number) { this.minPrice = val; }
  get currentMax(): number { return this.maxPrice ?? this.sliderMax; }
  set currentMax(val: number) { this.maxPrice = val; }

  //  Visual Logic (CSS Calculations)
  // Calculates the starting position (left %) of the colored track
  getLeftPercent(): string {
    const min = this.minPrice ?? 0;
    return ((min / this.sliderMax) * 100) + '%';
  }

  // Calculates the width (%) of the colored track based on the range.
  getWidthPercent(): string {
    const min = this.minPrice ?? 0;
    let max = this.maxPrice ?? this.sliderMax;

    // Cap the visual width at 100% if the user exceeds the slider max
    if (max > this.sliderMax) max = this.sliderMax;

    return (((max - min) / this.sliderMax) * 100) + '%';
  }

  // Validation Logic

  /*
   Prevents the user from typing invalid characters into number inputs.
    Blocks: '-', '+', 'e', '.', ','
   */
  preventInvalidChars(event: KeyboardEvent): void {
    const invalidChars = ['-', '+', 'e', '.', ','];
    if (invalidChars.includes(event.key)) {
      event.preventDefault();
    }
  }

  validateInput(isMin: boolean) {
    let value = isMin ? this.minPrice : this.maxPrice;

    if (value === null || value === undefined) return;

    // Enforce non-negative values
    if (value < 0) value = 0;

    // Enforce integers
    value = Math.round(value);

    // Update state
    if (isMin) this.minPrice = value;
    else this.maxPrice = value;

    // Ensure logical consistency between min and max
    this.validateRange(isMin);
  }

  validateRange(isMin: boolean) {
    let min = this.minPrice ?? 0;
    let max = this.maxPrice ?? this.sliderMax;

    if (isMin) {
      // Prevent min from exceeding max (accounting for gap)
      if (min > max - this.minGap) {
        this.minPrice = max - this.minGap;
        if (this.minPrice < 0) this.minPrice = 0;
      }
    } else {
      // Prevent max from dropping below min (accounting for gap)
      if (max < min + this.minGap) {
        this.maxPrice = min + this.minGap;
      }
    }
  }
}
