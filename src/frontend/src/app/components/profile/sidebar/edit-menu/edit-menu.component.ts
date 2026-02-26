import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../../../services/auth.service';
import { MenuService } from '../../../../services/menu.service';

type MenuApi = {
  menu_ID?: number;
  id?: number;
  title?: string;
  description?: string;
  pricePerPerson?: number;
  minNumberDiners?: number;
  maxNumberDiners?: number;
  kitchenRequirements?: string;
  deliveryAvailable?: boolean;
  cookAtClientHome?: boolean;
  pickupAvailable?: boolean;
  dishes?: DishApi[];
};

type DishApi = {
  dishId?: number;
  menuId?: number;
  title?: string;
  description?: string;
  category?: string;
  allergens?: string[];
};

type EditableDish = {
  dishId?: number;
  title: string;
  description: string;
  category: string;
  allergenIds: number[];
  photo?: File | null;
  isNew?: boolean;
};

@Component({
  selector: 'app-edit-menu',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-menu.component.html',
  styleUrls: ['./edit-menu.component.css']
})
export class EditMenuComponent implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private menuService = inject(MenuService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private destroy$ = new Subject<void>();
  isLoading = true;
  isSaving = false;
  errorMessage = '';
  successMessage = '';
  showConfirmDialog = false;
  confirmAction: 'delete-menu' | 'delete-dish' | null = null;
  confirmDishIndex = -1;

  menuId = 0;

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
    pricePerPerson: 0,
    minNumberDiners: 2,
    maxNumberDiners: 10,
    kitchenRequirements: '',
    deliveryAvailable: false,
    cookAtClientHome: false,
    pickupAvailable: false
  };

  dishes: EditableDish[] = [];
  kitchenTags: string[] = [];
  currentTagInput = '';

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (!user || user.role !== 'CHEF') {
      this.router.navigate(['/login']);
      return;
    }

    const idParam = this.route.snapshot.paramMap.get('id');
    this.menuId = idParam ? Number(idParam) : 0;

    if (!this.menuId) {
      this.errorMessage = 'No se pudo identificar el menú seleccionado.';
      this.isLoading = false;
      return;
    }

    this.loadMenu();
  }

  loadMenu(): void {
    this.isLoading = true;
    this.errorMessage = '';
    console.log('EditMenu: Cargando menú con ID:', this.menuId);
    this.menuService.getMenusByChef().pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (menus) => {
        console.log('EditMenu: Menús obtenidos del servidor:', menus);
        const found = menus.find(menu => menu.menu_ID === this.menuId || menu.id === this.menuId);

        if (!found) {
          console.error('EditMenu: Menú no encontrado con ID:', this.menuId);
          this.errorMessage = 'No encontramos el menú. Vuelve a tu carta y selecciona otro.';
          this.isLoading = false;
          this.cdr.detectChanges();
          return;
        }

        console.log('EditMenu: Menú encontrado:', found);
        this.menuForm = {
          title: found.title || '',
          description: found.description || '',
          pricePerPerson: Number(found.pricePerPerson || 0),
          minNumberDiners: Number(found.minNumberDiners || 1),
          maxNumberDiners: Number(found.maxNumberDiners || 1),
          kitchenRequirements: found.kitchenRequirements || '',
          deliveryAvailable: !!found.deliveryAvailable,
          cookAtClientHome: !!found.cookAtClientHome,
          pickupAvailable: !!found.pickupAvailable
        };

        this.kitchenTags = this.menuForm.kitchenRequirements
          ? this.menuForm.kitchenRequirements.split(',').map(tag => tag.trim()).filter(tag => tag)
          : [];

        const menuDishes = Array.isArray(found.dishes) ? found.dishes : [];
        this.dishes = menuDishes.map((dish: DishApi) => ({
          dishId: dish.dishId,
          title: dish.title || '',
          description: dish.description || '',
          category: dish.category || 'Plato Principal',
          allergenIds: this.mapAllergenNamesToIds(dish.allergens || [])
        }));

        console.log('EditMenu: Platos cargados:', this.dishes.length);
        if (this.dishes.length === 0) {
          this.addDish();
        }

        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('EditMenu: Error cargando menú:', err);
        this.errorMessage = 'No pudimos cargar tu menú. Revisa tu conexión e inténtalo de nuevo.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  isFormValid(): boolean {
    const isMenuOk =
      this.menuForm.title.trim() &&
      this.menuForm.description.trim() &&
      this.menuForm.pricePerPerson >= 1 &&
      this.menuForm.minNumberDiners >= 1 &&
      this.menuForm.maxNumberDiners >= 1 &&
      Number.isInteger(this.menuForm.minNumberDiners) &&
      this.menuForm.maxNumberDiners >= this.menuForm.minNumberDiners;

    const areDishesOk = this.dishes.length > 0 && this.dishes.every(dish =>
      dish.title.trim() !== '' &&
      dish.description.trim() !== '' &&
      dish.allergenIds.length > 0
    );

    return !!(isMenuOk && areDishesOk);
  }

  saveMenu(): void {
    if (!this.isFormValid()) return;

    this.isSaving = true;
    console.log('EditMenu: Guardando menú con ID:', this.menuId);

    this.syncKitchenRequirements();

    const payload = {
      id: this.menuId,
      title: this.menuForm.title.trim(),
      description: this.menuForm.description.trim(),
      pricePerPerson: this.menuForm.pricePerPerson,
      minNumberDiners: this.menuForm.minNumberDiners,
      maxNumberDiners: this.menuForm.maxNumberDiners,
      kitchenRequirements: this.menuForm.kitchenRequirements,
      deliveryAvailable: this.menuForm.deliveryAvailable,
      cookAtClientHome: this.menuForm.cookAtClientHome,
      pickupAvailable: this.menuForm.pickupAvailable
    };

    this.menuService.updateMenu(payload).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        console.log('EditMenu: Menú actualizado exitosamente, guardando platos...');
        this.saveDishes();
      },
      error: (err) => {
        console.error('EditMenu: Error al guardar menú:', err);
        this.isSaving = false;
        this.errorMessage = 'No se pudo guardar el menú. Inténtalo de nuevo.';
        this.cdr.detectChanges();
      }
    });
  }

  deleteMenu(): void {
    this.confirmAction = 'delete-menu';
    this.showConfirmDialog = true;
  }

  confirmDeleteMenu(): void {
    this.showConfirmDialog = false;
    console.log('EditMenu: Eliminando menú con ID:', this.menuId);
    this.menuService.deleteMenu(this.menuId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        console.log('EditMenu: Menú eliminado exitosamente, redirigiendo a /profile/menus');
        this.successMessage = 'Menú eliminado exitosamente.';
        this.cdr.detectChanges();
        setTimeout(() => this.router.navigate(['/profile/menus']), 1500);
      },
      error: (err) => {
        console.error('EditMenu: Error al eliminar menú:', err);

        // Verificar status HTTP 403 (Forbidden) que indica que hay restricciones (reservas)
        if (err.status === 403) {
          this.errorMessage = 'No se puede eliminar este menú porque tiene reservas confirmadas. Por favor, espera a que finalicen todas las reservas antes de eliminar el menú.';
        } else {
          // Verificar si el backend envía mensaje con detalles
          const errorMsg = err.error?.message || err.error || err.message || '';
          if (errorMsg.includes('reservas confirmadas') ||
              errorMsg.includes('reservations') ||
              errorMsg.includes('foreign key constraint')) {
            this.errorMessage = 'No se puede eliminar este menú porque tiene reservas confirmadas. Por favor, espera a que finalicen todas las reservas antes de eliminar el menú.';
          } else {
            this.errorMessage = 'No se pudo eliminar el menú. Inténtalo de nuevo.';
          }
        }
        this.cdr.detectChanges();
      }
    });
    this.confirmAction = null;
  }

  cancelConfirmDialog(): void {
    this.showConfirmDialog = false;
    this.confirmAction = null;
    this.confirmDishIndex = -1;
  }

  cancel(): void {
    this.router.navigate(['/profile/menus']);
  }

  addKitchenTag(event: Event): void {
    event.preventDefault();

    const value = this.currentTagInput.trim();
    if (value && !this.kitchenTags.includes(value)) {
      this.kitchenTags.push(value);
      this.syncKitchenRequirements();
    }

    this.currentTagInput = '';
  }

  removeKitchenTag(index: number): void {
    this.kitchenTags.splice(index, 1);
    this.syncKitchenRequirements();
  }

  addDish(): void {
    if (this.dishes.length >= 6) return;

    this.dishes.push({
      title: '',
      description: '',
      category: 'Plato Principal',
      allergenIds: [],
      photo: null,
      isNew: true
    });
  }

  removeDish(index: number): void {
    if (this.dishes.length === 1) {
      this.errorMessage = 'Debes mantener al menos un plato en el menú.';
      this.cdr.detectChanges();
      return;
    }

    const dish = this.dishes[index];
    if (!dish) return;

    if (!dish.dishId) {
      this.dishes.splice(index, 1);
      this.errorMessage = '';
      this.cdr.detectChanges();
      return;
    }

    this.confirmAction = 'delete-dish';
    this.confirmDishIndex = index;
    this.showConfirmDialog = true;
  }

  confirmDeleteDish(): void {
    this.showConfirmDialog = false;
    const index = this.confirmDishIndex;
    const dish = this.dishes[index];

    console.log('EditMenu: Eliminando plato con ID:', dish.dishId);
    this.menuService.deleteDish(this.menuId, dish.dishId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        console.log('EditMenu: Plato eliminado exitosamente');
        this.dishes.splice(index, 1);
        this.errorMessage = '';
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('EditMenu: Error al eliminar plato:', err);
        this.errorMessage = 'No se pudo eliminar el plato. Intentalo de nuevo.';
        this.cdr.detectChanges();
      }
    });
    this.confirmAction = null;
    this.confirmDishIndex = -1;
    });
  }

  toggleAllergen(dishIndex: number, allergenId: number): void {
    const ids = this.dishes[dishIndex].allergenIds;
    const index = ids.indexOf(allergenId);
    if (index > -1) ids.splice(index, 1);
    else ids.push(allergenId);
  }

  onFileSelected(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    const file = input.files ? input.files[0] : null;
    if (!file) return;

    if (file.size > 2 * 1024 * 1024) {
      alert('La imagen es demasiado pesada. Maximo 2MB.');
      if (input) input.value = '';
      return;
    }

    this.dishes[index].photo = file;
  }

  blockSomeKeys(event: KeyboardEvent): void {
    if (event.key === '.' || event.key === ',' || event.key === '-' || event.key === '+' || event.key === '0' || event.key === 'e' || event.key === 'E') {
      event.preventDefault();
    }
  }

  sanitizeInteger(field: 'minNumberDiners' | 'maxNumberDiners'): void {
    const value = this.menuForm[field];
    if (value) {
      this.menuForm[field] = Math.round(value);
    }
  }

  private syncKitchenRequirements(): void {
    this.menuForm.kitchenRequirements = this.kitchenTags.join(', ');
  }

  private mapAllergenNamesToIds(names: string[]): number[] {
    return names
      .map(name => this.officialAllergens.find(allergen => allergen.name === name)?.id)
      .filter((id): id is number => typeof id === 'number');
  }

  private mapAllergenIdsToNames(ids: number[]): string[] {
    return ids
      .map(id => this.officialAllergens.find(allergen => allergen.id === id)?.name)
      .filter((name): name is string => typeof name === 'string');
  }

  private saveDishes(): void {
    const requests: Observable<any>[] = [];
    let nextDishId = this.getMaxDishId();

    this.dishes.forEach(dish => {
      const allergens = this.mapAllergenIdsToNames(dish.allergenIds);

      if (dish.dishId) {
        requests.push(this.menuService.updateDish({
          menuId: this.menuId,
          dishId: dish.dishId,
          title: dish.title.trim(),
          description: dish.description.trim(),
          category: dish.category,
          allergens
        }));
      } else {
        nextDishId += 1;
        dish.dishId = nextDishId;
        requests.push(this.menuService.createDish({
          menu_ID: this.menuId,
          dish_ID: dish.dishId,
          title: dish.title.trim(),
          description: dish.description.trim(),
          category: dish.category,
          allergens
        }));
      }
    });

    if (requests.length === 0) {
      console.log('EditMenu: No hay platos para guardar, redirigiendo...');
      this.isSaving = false;
      this.router.navigate(['/profile/menus']);
      return;
    }

    console.log('EditMenu: Guardando', requests.length, 'platos...');
    forkJoin(requests).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        console.log('EditMenu: Todos los platos guardados exitosamente, redirigiendo...');
        this.isSaving = false;
        this.router.navigate(['/profile/menus']);
      },
      error: (err) => {
        console.error('EditMenu: Error al guardar platos:', err);
        this.isSaving = false;
        this.errorMessage = 'No se pudieron guardar los platos. Intentalo de nuevo.';
        this.cdr.detectChanges();
      }
    });
  }

  private getMaxDishId(): number {
    const ids = this.dishes
      .map(dish => dish.dishId || 0)
      .filter(id => id > 0);
    return ids.length ? Math.max(...ids) : 0;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
