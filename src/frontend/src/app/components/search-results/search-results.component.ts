import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchFilterComponent } from '../search-filter/search-filter.component';
import { ChefService } from '../../services/search-results.service';
import { Chef, ChefFilter } from '../../models/search-results.model';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-search-results',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './search-results.component.html',
  styleUrl: './search-results.component.css'
})
export class SearchResultsComponent implements OnInit {

  // DI
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private chefService = inject(ChefService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  // variables
  chefs: Chef[] = [];
  isLoading = true;
  currentUser: User | null = null;
  showLoginModal = false;
  searchTerm: string = '';

  ngOnInit() {
    console.log('COMPONENTE INICIADO: SearchResults');

    // user subscription
    this.authService.user$.subscribe(user => {
      this.currentUser = user;
    });

    // URL subscription (search + filters)
    this.route.queryParams.subscribe(params => {
      console.log('URL CAMBIÓ:', params);

      this.searchTerm = params['q'] || '';

      const filters: ChefFilter = {
        searchText: params['q'] || '',
        date: params['date'] || null,
        minPrice: params['min'] ? Number(params['min']) : null,
        maxPrice: params['max'] ? Number(params['max']) : null,
        guestCount: params['guests'] ? Number(params['guests']) : null,
        dietIds: params['diets'] ? params['diets'].split(',').map(Number) : [],
        onlyTopRated: params['top'] === 'true'
      };

      this.performSearch(filters);
    });
  }

  performSearch(filters: ChefFilter) {
    this.isLoading = true;
    this.cdr.detectChanges();

    console.log('BUSCANDO CHEFS...', filters);

    this.chefService.searchChefs(filters).subscribe({
      next: (pageData) => {
        console.log('DATOS RECIBIDOS:', pageData.content);
        this.chefs = pageData.content || [];
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('ERROR:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  goToChefDetail(id: number) {
    this.router.navigate(['/service-detail', 'chef', id]);
  }

  onReserve(chef: Chef) {
    if (!this.currentUser) {
      this.showLoginModal = true;
      return;
    }

    if (confirm(`¿Solicitar comanda a ${chef.name}?`)) {
      alert('¡Solicitud enviada correctamente!');
    }
  }

  // modal methods
  closeLoginModal() {
    this.showLoginModal = false;
  }

  goToLogin() {
    this.showLoginModal = false;
    this.router.navigate(['/signIn']);
  }
}
