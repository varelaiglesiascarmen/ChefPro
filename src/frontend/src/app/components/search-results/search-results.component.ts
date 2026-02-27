import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ChefService } from '../../services/search-results.service';
import { ChefSearchResult, MenuSearchResult, ChefFilter } from '../../models/search-results.model';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-search-results',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './search-results.component.html',
  styleUrl: './search-results.component.css'
})
export class SearchResultsComponent implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private chefService = inject(ChefService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);
  private cdr = inject(ChangeDetectorRef);

  chefs: ChefSearchResult[] = [];
  menus: MenuSearchResult[] = [];
  noResults: boolean = false;
  isLoading = true;
  currentUser: User | null = null;
  showLoginModal = false;
  searchTerm: string = '';

  ngOnInit() {
    this.authService.user$.subscribe(user => {
      this.currentUser = user;
    });

    this.route.queryParams.subscribe(params => {
      this.searchTerm = params['q'] || '';

      const filters: ChefFilter = {
        q:          params['q']      || '',
        date:       params['date']   || null,
        minPrice:   params['min']    ? Number(params['min'])    : null,
        maxPrice:   params['max']    ? Number(params['max'])    : null,
        guests:     params['guests'] ? Number(params['guests']) : null,
        allergens:  params['allergens'] ? params['allergens'].split(',') : []
      };

      this.performSearch(filters);
    });
  }

  performSearch(filters: ChefFilter) {
    this.isLoading = true;
    this.cdr.detectChanges();

    this.chefService.searchChefs(filters).subscribe({
      next: (data) => {
        this.chefs = data.chefs || [];
        this.menus = data.menus || [];
        this.noResults = data.noResults ?? false;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chefs = [];
        this.menus = [];
        this.noResults = false;
        this.isLoading = false;
        this.toastService.error('No pudimos completar la búsqueda. Intenta nuevamente.');
        this.cdr.detectChanges();
      }
    });
  }

  get totalResults(): number {
    return this.chefs.length + this.menus.length;
  }

  goToChefDetail(chefId: number) {
    this.router.navigate(['/service-detail', 'chef', chefId]);
  }

  goToMenuDetail(menuId: number) {
    this.router.navigate(['/service-detail', 'menu', menuId]);
  }

  onReserveChef(chef: ChefSearchResult) {
    if (!this.currentUser) {
      this.showLoginModal = true;
      return;
    }
    this.toastService.info('Selecciona fecha y detalles en la ficha del chef para completar la reserva.');
    this.goToChefDetail(chef.id);
  }

  onReserveMenu(menu: MenuSearchResult) {
    if (!this.currentUser) {
      this.showLoginModal = true;
      return;
    }
    this.toastService.info('Selecciona fecha y comensales en el detalle del menú para reservar.');
    this.goToMenuDetail(menu.menuId);
  }

  closeLoginModal() {
    this.showLoginModal = false;
  }

  goToLogin() {
    this.showLoginModal = false;
    this.router.navigate(['/signIn']);
  }
}
