import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';
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

  user: User | null = null;
  currentUser: User | null = null;
  showLoginModal = false;
  viewType: 'chef' | 'menu' | null = null;
  isLoading = true;
  data: any = null;
  reservationDate: string = '';
  reservationGuests: number = 2;
  selectedMenuId: number | null = null;
  isDropdownOpen: boolean = false;
  officialAllergens = OFFICIAL_ALLERGENS;

  ngOnInit() {
    this.authService.user$.subscribe(user => {
      this.currentUser = user;
    });

    this.route.paramMap.subscribe(params => {
      const type = params.get('type');
      const id = Number(params.get('id'));

      this.isLoading = true;
      this.reservationDate = '';
      this.selectedMenuId = null;
      this.isDropdownOpen = false;

      setTimeout(() => {
        if (type === 'chef') {
          this.viewType = 'chef';
          this.loadChefFromDB(id);
        } else if (type === 'menu') {
          this.viewType = 'menu';
          this.loadMenuFromDB(id);
        } else {
          this.router.navigate(['/']);
        }
        this.isLoading = false;
      }, 200);
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
    console.log('El usuario eligió:', fechaRecibida);
    this.reservationDate = fechaRecibida;
  }

  requestReservation() {
    if (!this.currentUser) {
      this.showLoginModal = true;
      return;
    }

    const unitPrice = this.viewType === 'chef' ? this.selectedMenu?.price : this.data.price;
    const totalPrice = (unitPrice * this.reservationGuests).toFixed(2);
    const reservationData = {
      chef_ID: this.viewType === 'chef' ? this.data.id : this.data.chefId,
      diner_ID: this.currentUser.user_ID,
      menu_ID: this.viewType === 'chef' ? this.selectedMenuId : this.data.id,
      date: this.reservationDate,
      n_diners: this.reservationGuests,
      total_price: totalPrice,
      address: this.currentUser.address,
      status: 'PENDING'
    };

    console.log('Enviando a Backend:', reservationData);

    alert(`Solicitud enviada por ${totalPrice}€ a la dirección: ${reservationData.address}`);
  }

  closeLoginModal() { this.showLoginModal = false; }

  goToLogin() {
    this.showLoginModal = false;
    this.router.navigate(['/login']);
  }

  loadChefFromDB(id: number) {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = tomorrow.toISOString().split('T')[0];

    const allReviewsFromDB = [
      { user: 'Juan P.', date: '10/01/2026', rating: 5, text: 'Increíble experiencia.' },
      { user: 'Ana L.', date: '12/01/2026', rating: 5, text: 'La mejor cena en casa.' },
      { user: 'Lucía G.', date: '20/01/2026', rating: 5, text: 'El risotto espectacular.' },
      { user: 'Pedro S.', date: '22/01/2026', rating: 5, text: 'Profesionalidad pura.' },
      { user: 'Sofía T.', date: '01/02/2026', rating: 5, text: 'Un 10 absoluto.' },
      { user: 'Diego F.', date: '03/02/2026', rating: 5, text: 'Gordon es super amable.' },
      { user: 'Elena V.', date: '04/02/2026', rating: 4, text: 'Muy rico todo.' },
      { user: 'Javier B.', date: '06/02/2026', rating: 5, text: 'Lujo total.' },
      { user: 'Laura D.', date: '07/02/2026', rating: 5, text: 'Exquisito.' },
      { user: 'Marina L.', date: '09/02/2026', rating: 5, text: 'Una velada inolvidable.' }
    ];

    this.data = {
      id: id,
      name: 'Gordon',
      lastname: 'Ramz',
      fullName: 'Gordon Ramz',
      email: 'gordon@kitchen.com',
      photoUrl: 'https://images.unsplash.com/photo-1577219491135-ce391730fb2c',
      bio: 'Pasión por la cocina internacional y la perfección.',
      prizes: ['3 Estrellas Michelin', 'Chef Mejor Valorado 2026'],
      rating: 4.9,
      reviewsCount: 124,
      location: 'Disponible en Madrid',
      languages: ['Español', 'Inglés', 'Francés'],
      coverUrl: 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0',
      busyDates: [tomorrowStr, '2026-02-20', '2026-02-28'],

      menus: [
        {
          id: 1,
          title: 'Italia Clásica',
          price: 55.00,
          dishesCount: 4,
          description: 'Un viaje gastronómico por la Toscana.',
          minDiners: 2,
          maxDiners: 8
        },
        {
          id: 2,
          title: 'Aires del Sur',
          price: 80.50,
          dishesCount: 6,
          description: 'Lo mejor del mar y la tierra andaluza.',
          minDiners: 4,
          maxDiners: 15
        }
      ],
      reviewsList: allReviewsFromDB
    };
  }

  loadMenuFromDB(id: number) {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = tomorrow.toISOString().split('T')[0];
    const commonBusyDates = [tomorrowStr, '2026-02-25'];

    if (id === 1) {
      this.data = {
        id: 1, title: 'Italia Clásica', description: 'Un recorrido apasionante por la Toscana.',
        price: 55.00, minDiners: 2, maxDiners: 8, requirements: 'Horno y 4 fuegos',
        chefId: 2, chefName: 'Gordon Ramz',
        photoUrl: 'https://images.unsplash.com/photo-1595295333158-4742f28fbd85',
        busyDates: commonBusyDates,
        dishes: [
          { title: 'Bruschetta', description: 'Pan con tomate.', category: 'Entrante', allergenIds: [1] },
          { title: 'Risotto', description: 'Arroz con boletus.', category: 'Principal', allergenIds: [7, 12] },
          { title: 'Panna Cotta', description: 'Con frutos rojos.', category: 'Postre', allergenIds: [7] }
        ],
        allergens: ['Gluten', 'Lácteos']
      };
      this.reservationGuests = this.data.minDiners;

    } else if (id === 2) {
      this.data = {
        id: 2, title: 'Aires del Sur', description: 'La frescura del mar y la tierra.',
        price: 80.50, minDiners: 4, maxDiners: 12, requirements: 'Fuegos de gas',
        chefId: 2, chefName: 'Gordon Ramz',
        photoUrl: 'https://images.unsplash.com/photo-1534939561126-855b8675edd7',
        busyDates: commonBusyDates,
        dishes: [
          { title: 'Salmorejo', description: 'Con jamón.', category: 'Entrante', allergenIds: [1, 3] },
          { title: 'Urta', description: 'Pescado de roca.', category: 'Principal', allergenIds: [4] },
          { title: 'Tocino de Cielo', description: 'Postre.', category: 'Postre', allergenIds: [3] }
        ],
        allergens: ['Gluten', 'Huevo', 'Pescado']
      };
      this.reservationGuests = this.data.minDiners;
    } else {
      this.data = { id: id, title: 'Menú Standard', price: 50, dishes: [] };
    }
  }

  goBack() {
    if (window.history.length > 1) this.location.back();
    else this.router.navigate(['/search-results']);
  }

  goToMenu(id: number) { this.router.navigate(['/service-detail', 'menu', id]); }
  goToChefProfile(id: number) { this.router.navigate(['/service-detail', 'chef', id]); }
}
