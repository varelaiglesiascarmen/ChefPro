import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, forkJoin, from, switchMap } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { ChefService } from '../../services/chef.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-new-menu',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './new-menu.component.html',
  styleUrls: ['./new-menu.component.css']
})
export class NewMenuComponent implements OnInit {
  private authService = inject(AuthService);
  private chefService = inject(ChefService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  officialAllergens = [
    { id: 1, name: 'Gluten' }, { id: 2, name: 'Crustáceos' },
    { id: 3, name: 'Huevos' }, { id: 4, name: 'Pescado' },
    { id: 5, name: 'Cacahuetes' }, { id: 6, name: 'Soja' },
    { id: 7, name: 'Lácteos' }, { id: 8, name: 'Frutos de cáscara' },
    { id: 9, name: 'Apio' }, { id: 10, name: 'Mostaza' },
    { id: 11, name: 'Granos de sésamo' }, { id: 12, name: 'Dióxido de azufre y sulfitos' },
    { id: 13, name: 'Altramuces' }, { id: 14, name: 'Moluscos' }
  ];

  menuForm = {
    title: '',
    description: '',
    price_per_person: 0,
    min_number_diners: 2,
    max_number_diners: 10,
    kitchen_requirements: ''
  };

  dishes: any[] = [];
  kitchenTags: string[] = [];
  currentTagInput: string = '';
  isSaving = false;

  ngOnInit() {
    // We verify that only users of the chef type can see this interface. If not, we redirect to login.
    const user = this.authService.currentUserValue;
    if (!user || user.role !== 'CHEF') {
      this.router.navigate(['/login']);
      return;
    }
    this.addDish();
  }

  // save skills form kitchen_requirements as an array of tags, but keep the database field as a comma-separated string for compatibility with the existing schema
  addKitchenTag(event: any): void {
    event.preventDefault();

    const value = this.currentTagInput.trim();
    if (value && !this.kitchenTags.includes(value)) {
      this.kitchenTags.push(value);
      // Synchronize the database field (separating skills with commas)
      this.menuForm.kitchen_requirements = this.kitchenTags.join(', ');
    }
    this.currentTagInput = '';
  }

  removeKitchenTag(index: number): void {
    this.kitchenTags.splice(index, 1);
    this.menuForm.kitchen_requirements = this.kitchenTags.join(', ');
  }

  addDish() {
    if (this.dishes.length < 6) {
      this.dishes.push({
        title: '',
        description: '',
        category: 'Plato Principal',
        allergenIds: [],
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

  // UX logic: Only 1 photo per dish and max. 2MB
  onFileSelected(event: any, index: number) {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        this.toastService.error('La imagen es demasiado pesada. Máximo 2MB.');
        event.target.value = '';
        return;
      }
      this.dishes[index].photo = file;
    }
  }

  // validate obligatory fields and that the number of diners is correct
  isFormValid(): boolean {
    // Validate basic menu data
    const isMenuOk =
      this.menuForm.title.trim() !== '' &&
      this.menuForm.price_per_person >= 1 &&
      this.menuForm.min_number_diners >= 1 &&
      this.menuForm.max_number_diners >= 1 &&
      Number.isInteger(this.menuForm.min_number_diners) &&
      this.menuForm.max_number_diners >= this.menuForm.min_number_diners;

    // check that the dishes are complete
    const areDishesOk = this.dishes.length > 0 && this.dishes.every(d =>
      d.title.trim() !== '' &&
      d.description.trim() !== '' &&
      d.allergenIds.length > 0
    );

    return !!(isMenuOk && areDishesOk);
  }

  // block letters and symbols in the form
  blockSomeKeys(event: KeyboardEvent): void {
    if (event.key === '.' || event.key === ',' || event.key === '-' || event.key === '+' || event.key === '0' || event.key === 'e' || event.key === 'E') {
      event.preventDefault();
    }
  }

  // round up if the user manages to enter a decimal point.
  sanitizeInteger(field: 'min_number_diners' | 'max_number_diners'): void {
    const value = this.menuForm[field];
    if (value) {
      this.menuForm[field] = Math.round(value);
    }
  }

  saveMenu() {
    if (!this.isFormValid() || this.isSaving) {
      return;
    }

    const chefId = this.authService.currentUserValue?.user_ID;
    if (!chefId) {
      this.toastService.error('No se pudo identificar al chef autenticado.');
      return;
    }

    this.isSaving = true;

    const menuPayload = { ...this.menuForm, chef_ID: chefId };

    this.chefService.createMenu(menuPayload).subscribe({
      next: (resMenu: any) => {
        const newMenuId = resMenu.menu_ID;

        from(Promise.all(this.dishes.map(async (dish, index) => {
          const allergenNames = dish.allergenIds.map((id: number) => {
            const allergen = this.officialAllergens.find(a => a.id === id);
            return allergen ? allergen.name : null;
          }).filter((name: string | null) => name !== null);

          const photoBase64 = dish.photo ? await this.fileToBase64(dish.photo) : null;

          return {
            menu_ID: newMenuId,
            dish_ID: index + 1,
            title: dish.title,
            description: dish.description,
            category: dish.category,
            allergens: allergenNames,
            photo: photoBase64
          };
        }))).pipe(
          switchMap((dishesPayload) => forkJoin(dishesPayload.map(dishPayload => this.chefService.createDish(dishPayload)))),
          finalize(() => {
            this.isSaving = false;
          })
        ).subscribe({
          next: () => {
            this.toastService.success('¡Menú creado con éxito!');
            this.router.navigate(['/profile']);
          },
          error: () => {
            this.toastService.error('No pudimos crear todos los platos del menú. Intenta nuevamente.');
          }
        });
      },
      error: () => {
        this.isSaving = false;
        this.toastService.error('Error al crear el menú base. Revisa tu conexión e inténtalo de nuevo.');
      }
    });
  }

  private fileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(String(reader.result ?? ''));
      reader.onerror = () => reject(new Error('Error al procesar la imagen'));
      reader.readAsDataURL(file);
    });
  }
}
