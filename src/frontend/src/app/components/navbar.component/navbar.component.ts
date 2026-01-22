// importation of components, services, directives, and modules
import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { FocusOnInitDirective } from '../../directives/focus-on-init.directive';
import { SearchFilterComponent } from '../search-filter.component/search-filter.component';

// definition of the NavbarComponent
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    FormsModule,
    CommonModule,
    FocusOnInitDirective,
    SearchFilterComponent
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
// exportation of the NavbarComponent class
export class NavbarComponent {

  private authService = inject(AuthService);
  private router = inject(Router);

  isMenuOpen = false;
  isSearchOpen = false;
  searchText: string = '';
  showFilterMenu = false;
  hasFilterActive = false;

  // Getter to retrieve the user's profile image or a default image
  get userProfileImage(): string {
    const defaultImage = '/logos/users.svg';

    if (this.authService.isLoggedIn()) {
      const user = this.authService.currentUser;
      if (user && user.photoURL) {
        return user.photoURL;
      }
    }
    return defaultImage;
  }

  // Method to toggle the navigation menu
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

  // Method to toggle the search bar
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

  // Method to perform a search
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

  // Method to toggle the filter menu
  toggleFilterMenu() {
    this.showFilterMenu = !this.showFilterMenu;
  }

  // Method to handle applied filters
  onFilterApplied(filtros: any) {
    this.showFilterMenu = false;
    this.hasFilterActive = true;
    this.performSearch(filtros);
  }

  // Method to handle image errors
  handleImageError(event: any) {
    event.target.src = '/logos/users.svg';
  }

  // Method to navigate to the user's profile or login page
  goToProfile() {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/perfil']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
