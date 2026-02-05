import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router'; // Importación vital
import { AuthService } from '../../services/auth.service';
import { ChefService } from '../../services/chef.service';

@Component({
  selector: 'app-create-menu',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-menu.component.html',
  styleUrls: ['./create-menu.component.css']
})
export class CreateMenuComponent implements OnInit {
  private authService = inject(AuthService);
  private chefService = inject(ChefService);
  private router = inject(Router); // Inyectamos el router para ir a /profile

  // Lista oficial con IDs (para que coincida con la vista de detalle que me has pasado)
  officialAllergens = [
    { id: 1, name: 'Cereales con gluten' }, { id: 2, name: 'Crustáceos' },
    { id: 3, name: 'Huevos' }, { id: 4, name: 'Pescado' },
    { id: 5, name: 'Cacahuetes' }, { id: 6, name: 'Soja' },
    { id: 7, name: 'Leche' }, { id: 8, name: 'Frutos de cáscara' },
    { id: 9, name: 'Apio' }, { id: 10, name: 'Mostaza' },
    { id: 11, name: 'Sésamo' }, { id: 12, name: 'Sulfitos' },
    { id: 13, name: 'Altramuces' }, { id: 14, name: 'Moluscos' }
  ];

  menuForm = {
    title: '',
    description: '',
    price_per_person: 0,
    min_number_diners: 2,
    max_number_diners: 10,
    kitchen_requirements: '' // Tu Task List para el Chef
  };

  dishes: any[] = [];

  ngOnInit() {
    // Verificamos rol: solo CHEF puede estar aquí
    const user = this.authService.currentUserValue;
    if (!user || user.role !== 'CHEF') {
      this.router.navigate(['/login']);
      return;
    }
    this.addDish(); // Empezamos con un plato
  }

  addDish() {
    if (this.dishes.length < 6) {
      this.dishes.push({
        title: '',
        description: '',
        category: 'Plato Principal',
        allergenIds: [], // Guardaremos los números [1, 7, etc.]
        photo: null
      });
    }
  }

  removeDish(index: number) {
    if (this.dishes.length > 1) this.dishes.splice(index, 1);
  }

  toggleAllergen(dishIndex: number, allergenId: number) {
    const ids = this.dishes[dishIndex].allergenIds;
    const index = ids.indexOf(allergenId);
    if (index > -1) ids.splice(index, 1);
    else ids.push(allergenId);
  }

  // Lógica UX: Solo 1 foto por plato y máx 2MB
  onFileSelected(event: any, index: number) {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        alert('La imagen es demasiado pesada. Máximo 2MB para no colapsar la BBDD.');
        event.target.value = '';
        return;
      }
      this.dishes[index].photo = file;
    }
  }

  // Lógica de Negocio: Validar campos obligatorios
  isFormValid(): boolean {
    const isMenuOk = this.menuForm.title && this.menuForm.price_per_person > 0;
    const areDishesOk = this.dishes.length > 0 && this.dishes.every(d =>
      d.title.trim() !== '' &&
      d.description.trim() !== '' &&
      d.allergenIds.length > 0 // Alérgenos obligatorios
    );
    return !!(isMenuOk && areDishesOk);
  }

  saveMenu() {
    if (!this.isFormValid()) return;

    const chefId = this.authService.currentUserValue?.user_ID;

    // 1. Enviamos el Menú (Endpoint 5)
    const menuPayload = { ...this.menuForm, chef_ID: chefId };

    this.chefService.createMenu(menuPayload).subscribe({
      next: (resMenu: any) => {
        const newMenuId = resMenu.menu_ID;

        // 2. Enviamos los platos a la API /plato (Endpoint 6)
        this.dishes.forEach((dish, index) => {
          const dishPayload = {
            menu_ID: newMenuId,
            dish_ID: index + 1,
            title: dish.title,
            description: dish.description,
            category: dish.category,
            allergenIds: dish.allergenIds // Enviamos los números [1, 7...]
          };

          this.chefService.createDish(dishPayload).subscribe();
        });

        alert('✅ ¡Menú creado con éxito!');
        this.router.navigate(['/profile']); // Redirigimos al perfil privado del chef
      },
      error: (err) => alert('Error al crear el menú base. Revisa la conexión.')
    });
  }
}
