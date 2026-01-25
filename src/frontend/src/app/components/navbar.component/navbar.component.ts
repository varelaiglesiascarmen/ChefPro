import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { trigger, style, animate, transition } from '@angular/animations';
import { AuthService } from '../../services/auth.service';
import { FocusOnInitDirective } from '../../directives/focus-on-init.directive';
import { SearchFilterComponent } from '../search-filter.component/search-filter.component';
import { User } from '../../models/auth.model';
import { UserMenuComponent } from '../user-menu.component/user-menu.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    FormsModule,
    CommonModule,
    FocusOnInitDirective,
    SearchFilterComponent,
    UserMenuComponent
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
  animations: [
    trigger('fadeInOut', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-10px)' }),
        animate('200ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ]),
      transition(':leave', [
        animate('150ms ease-in', style({ opacity: 0, transform: 'translateY(-10px)' }))
      ])
    ])
  ]
})
export class NavbarComponent implements OnInit {

  private authService = inject(AuthService);
  private router = inject(Router);

  isMenuOpen = false;
  isSearchOpen = false;
  searchText: string = '';
  showFilterMenu = false;
  hasFilterActive = false;
  isUserMenuOpen = false;

  // This variable stores the current user.
  currentUser: User | null = null;

  // reactive subscription
  ngOnInit() {
    // We subscribe to the service. Every time the user changes (log in/log out),
    // this variable will update itself.
    this.authService.user$.subscribe(user => {
      this.currentUser = user;
    });
  }

  // get img of user active
  get userProfileImage(): string {
    const defaultImage = '/logos/users.svg';

    if (this.currentUser && this.currentUser.photoUrl) {
      return this.currentUser.photoUrl;
    }

    return defaultImage;
  }

  // Toggle menu
  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
    if (this.isMenuOpen) {
      this.isSearchOpen = false;
      this.showFilterMenu = false;
    }
  }

  closeMenu() {
    this.isMenuOpen = false;
  }

  // Toggle search
  toggleSearch() {
    const isMobile = window.innerWidth <= 768;

    if (isMobile) {
      if (!this.isSearchOpen) {
        this.isSearchOpen = true;
        this.isMenuOpen = false;
      } else {
        const cleanText = this.searchText.trim();
        const hasContent = cleanText.length > 0 || this.hasFilterActive;

        if (hasContent) {
          this.performSearch();
        } else {
          this.closeSearch();
        }
      }
    } else {
      this.performSearch();
    }
  }

  closeSearch() {
    this.isSearchOpen = false;
    this.searchText = '';
    this.showFilterMenu = false;
  }

  performSearch(filtrosOpcionales: any = null) {
    const cleanText = this.searchText.trim();

    if (cleanText || this.hasFilterActive || filtrosOpcionales) {
      console.log('Ejecutando búsqueda:', {
        texto: cleanText,
        filtrosActivos: this.hasFilterActive,
        nuevosFiltros: filtrosOpcionales
      });
    }
  }

  toggleFilterMenu() {
    this.showFilterMenu = !this.showFilterMenu;
  }

  onFilterApplied(filtros: any) {
    this.showFilterMenu = false;
    this.hasFilterActive = true;
    this.performSearch(filtros);
  }

  handleImageError(event: any) {
    event.target.src = '/logos/users.svg';
  }

  handleUserClick() {
    // We check if the user exists
    if (this.currentUser) {
      // Toggle the user menu instead of navigating directly
      this.isUserMenuOpen = !this.isUserMenuOpen;
    } else {
      this.router.navigate(['/login']);
    }
  }

  closeUserMenu() {
    this.isUserMenuOpen = false;
  }
}
