import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { ChefService } from '../../services/chef.service';
import { ReservationService } from '../../services/reservation.service';
import { User } from '../../models/auth.model';
import { ReservationCreateDto } from '../../models/reservation.model';
import { ChefPublicDetail, MenuPublicDetail } from '../../models/chef-detail.model';
import { CalendarComponent } from '../calendar/calendar.component';

const OFFICIAL_ALLERGENS = [
  { id: 1, name: 'Cereales con gluten', desc: 'Trigo, centeno, cebada, avena...' },
  { id: 2, name: 'Crustáceos', desc: 'Cangrejos, langostas, gambas...' },
  { id: 3, name: 'Huevos', desc: 'Presentes en mayonesas, pastas...' },
  { id: 4, name: 'Pescado', desc: 'Incluido en pizzas, salsas...' },
  { id: 5, name: 'Cacahuetes (Maní)', desc: 'Mantecas, aceite, chocolate...' },
  { id: 6, name: 'Soja', desc: 'Tofu, salsas, postres...' },
  { id: 7, name: 'Leche', desc: 'Incluida la lactosa...' },
  { id: 8, name: 'Frutos de cáscara', desc: 'Almendras, avellanas, nueces...' },
  { id: 9, name: 'Apio', desc: 'En sopas, ensaladas...' },
  { id: 10, name: 'Mostaza', desc: 'Aliños, salsas, currys...' },
  { id: 11, name: 'Granos de sésamo', desc: 'Panes, aceites...' },
  { id: 12, name: 'Dióxido de azufre y sulfitos', desc: 'Conservantes...' },
  { id: 13, name: 'Altramuces', desc: 'Harinas y derivados.' },
  { id: 14, name: 'Moluscos', desc: 'Mejillones, almejas, calamares...' }
];

@Component({
  selector: 'app-service-detail-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CalendarComponent],
  templateUrl: './service-detail-page.component.html',
  styleUrl: './service-detail-page.component.css'
})
export class ServiceDetailPageComponent implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private location = inject(Location);
  private authService = inject(AuthService);
  private chefService = inject(ChefService);
  private reservationService = inject(ReservationService);
  private cdr = inject(ChangeDetectorRef);

  user: User | null = null;
  currentUser: User | null = null;
  showLoginModal = false;
  viewType: 'chef' | 'menu' | null = null;
  isLoading = true;
  errorMessage: string | null = null;
  data: any = null;
  reservationDate: string = '';
  reservationGuests: number = 2;
  selectedMenuId: number | null = null;
  isDropdownOpen: boolean = false;
  officialAllergens = OFFICIAL_ALLERGENS;

  reservationLoading = false;
  reservationConfirmed = false;
  reservationSuccess: string | null = null;
  reservationError: string | null = null;

  ngOnInit() {
    this.authService.user$.subscribe(user => {
      this.currentUser = user;
      this.cdr.detectChanges();
    });

    this.route.paramMap.subscribe(params => {
      const type = params.get('type');
      const id = Number(params.get('id'));

      this.isLoading = true;
      this.errorMessage = null;
      this.data = null;
      this.reservationDate = '';
      this.selectedMenuId = null;
      this.isDropdownOpen = false;

      if (type === 'chef') {
        this.viewType = 'chef';
        this.loadChefFromDB(id);
      } else if (type === 'menu') {
        this.viewType = 'menu';
        this.loadMenuFromDB(id);
      } else {
        this.router.navigate(['/']);
      }
    });
  }

  get selectedMenu() {
    if (!this.data || !this.data.menus) return null;
    return this.data.menus.find((m: any) => m.id == this.selectedMenuId);
  }

  getAllergenName(id: number): string {
    const allergen = this.officialAllergens.find(a => a.id === id);
    return allergen ? allergen.name : 'Desconocido';
  }

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  selectMenu(id: number) {
    this.selectedMenuId = id;
    this.isDropdownOpen = false;
    this.onMenuChange();
  }

  onMenuChange() {
    if (this.selectedMenu) {
      this.reservationGuests = this.selectedMenu.minDiners || 2;
    }
  }

  updateGuests(increment: number) {
    const newVal = this.reservationGuests + increment;

    let min = 2;
    let max = 20;

    if (this.viewType === 'chef' && this.selectedMenu) {
      min = this.selectedMenu.minDiners || 2;
      max = this.selectedMenu.maxDiners || 20;
    }

    else if (this.viewType === 'menu' && this.data) {
      min = this.data.minDiners || 2;
      max = this.data.maxDiners || 20;
    }

    if (newVal >= min && newVal <= max) {
      this.reservationGuests = newVal;
    }
  }

  updateDate(fechaRecibida: string) {
    this.reservationDate = fechaRecibida;
  }

  requestReservation() {
    if (!this.currentUser) {
      this.showLoginModal = true;
      return;
    }

    if (this.currentUser.role !== 'DINER') {
      this.reservationError = 'Solo los comensales pueden hacer reservas.';
      return;
    }

    const dto: ReservationCreateDto = {
      chefId: this.viewType === 'chef' ? this.data.id : this.data.chefId,
      menuId: this.viewType === 'chef' ? this.selectedMenuId! : this.data.id,
      date: this.reservationDate,
      numberOfDiners: this.reservationGuests,
      address: this.currentUser.address || ''
    };

    this.reservationLoading = true;
    this.reservationSuccess = null;
    this.reservationError = null;

    this.reservationService.createReservation(dto).subscribe({
      next: () => {
        this.reservationLoading = false;
        this.reservationConfirmed = true;
        this.reservationSuccess = '¡Reserva enviada con éxito! El chef confirmará en las próximas 24 h.';
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.reservationLoading = false;
        if (err.status === 400 || err.status === 409) {
          const serverMsg = typeof err.error === 'string' ? err.error : err.error?.message;
          this.reservationError = serverMsg || 'Este chef ya tiene una reserva para esa fecha.';
        } else if (err.status === 401) {
          this.reservationError = 'Tu sesión ha expirado. Vuelve a iniciar sesión.';
        } else if (err.status === 403 || err.status === 500) {
          // El backend no tiene @ExceptionHandler → las validaciones de negocio
          // (fecha duplicada, menú inválido, etc.) llegan como 403 o 500.
          this.reservationError = 'No se pudo completar la reserva. Es posible que esta fecha ya esté ocupada.';
        } else {
          this.reservationError = 'Error al crear la reserva. Inténtalo de nuevo.';
        }
        this.cdr.detectChanges();
      }
    });
  }

  closeLoginModal() { this.showLoginModal = false; }

  goToLogin() {
    this.showLoginModal = false;
    this.router.navigate(['/login']);
  }

  loadChefFromDB(id: number) {
    this.chefService.getChefPublicProfile(id).subscribe({
      next: (chef: ChefPublicDetail) => {
        // Mapear la respuesta del backend al formato esperado por el template
        const languagesArray = chef.languages
          ? chef.languages.split(',').map(l => l.trim())
          : [];

        this.data = {
          id: chef.id,
          name: chef.name,
          lastname: chef.lastname,
          fullName: chef.fullName,
          email: chef.email,
          photoUrl: chef.photo,
          bio: chef.bio,
          prizes: chef.prizes,
          rating: chef.rating,
          reviewsCount: chef.reviewsCount,
          location: chef.location ? 'Disponible en ' + chef.location : '',
          languages: languagesArray,
          coverUrl: chef.coverPhoto || chef.photo,
          busyDates: chef.busyDates || [],
          menus: (chef.menus || []).map(m => ({
            id: m.id,
            title: m.title,
            price: m.price,
            dishesCount: m.dishesCount,
            description: m.description,
            minDiners: m.minDiners,
            maxDiners: m.maxDiners
          })),
          reviewsList: (chef.reviews || []).map(r => ({
            user: r.reviewerName,
            date: r.date,
            rating: r.score,
            text: r.comment
          }))
        };
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        if (err.status === 404) {
          this.errorMessage = 'Chef no encontrado.';
        } else if (err.status === 403) {
          this.errorMessage = 'No se pudo acceder al perfil del chef. Recarga la pagina o inicia sesion de nuevo.';
        } else {
          this.errorMessage = 'Error al cargar los datos del chef.';
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadMenuFromDB(id: number) {
    this.chefService.getMenuPublicDetail(id).subscribe({
      next: (menu: MenuPublicDetail) => {
        this.data = {
          id: menu.id,
          title: menu.title,
          description: menu.description,
          price: menu.price,
          minDiners: menu.minDiners,
          maxDiners: menu.maxDiners,
          requirements: menu.requirements,
          chefId: menu.chefId,
          chefName: menu.chefName,
          photoUrl: menu.chefPhoto,
          busyDates: menu.busyDates || [],
          dishes: (menu.dishes || []).map(d => ({
            title: d.title,
            description: d.description,
            category: d.category,
            allergenIds: d.allergenIds || []
          }))
        };
        this.reservationGuests = this.data.minDiners || 2;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        if (err.status === 404) {
          this.errorMessage = 'Menú no encontrado.';
        } else if (err.status === 403) {
          this.errorMessage = 'No se pudo acceder al menu. Recarga la pagina o inicia sesion de nuevo.';
        } else {
          this.errorMessage = 'Error al cargar los datos del menú.';
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  goBack() {
    if (window.history.length > 1) this.location.back();
    else this.router.navigate(['/search-results']);
  }

  goToMenu(id: number) { this.router.navigate(['/service-detail', 'menu', id]); }
  goToChefProfile(id: number) { this.router.navigate(['/service-detail', 'chef', id]); }
}
