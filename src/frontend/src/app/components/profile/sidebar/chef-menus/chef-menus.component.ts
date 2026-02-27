import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MenuService } from '../../../../services/menu.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-chef-menus',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './chef-menus.component.html',
  styleUrls: ['./chef-menus.component.css']
})
export class ChefMenusComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  private menuService = inject(MenuService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);
  private destroy$ = new Subject<void>();

  menus: any[] = [];
  errorMessage = '';
  successMessage = '';
  showConfirmDialog = false;
  confirmMenuId = -1;

  ngOnInit() {
    this.loadMenus();
  }

  loadMenus() {
    this.menuService.getMenusByChef().pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (data) => {
        this.menus = [...data];
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No pudimos cargar tus menús. Revisa tu conexión e inténtalo de nuevo.';
        this.cdr.detectChanges();
      }
    });
  }

  confirmDelete(id: number) {
    this.confirmMenuId = id;
    this.showConfirmDialog = true;
  }

  confirmDeleteMenu(): void {
    const id = this.confirmMenuId;
    this.showConfirmDialog = false;
    this.menuService.deleteMenu(id).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        this.successMessage = 'Menú eliminado exitosamente.';
        this.cdr.detectChanges();
        setTimeout(() => {
          this.successMessage = '';
          this.loadMenus();
        }, 1000);
      },
      error: (err) => {

        // Verificar status HTTP 403 (Forbidden) que indica que hay restricciones (reservas)
        if (err.status === 403) {
          this.errorMessage = 'No se puede eliminar este menú porque tiene reservas confirmadas. Por favor, espera a que finalicen todas las reservas antes de eliminar el menú.';
        } else {
          // Verificar si el mensaje contiene información sobre reservas
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
    this.confirmMenuId = -1;
  }

  cancelConfirmDialog(): void {
    this.showConfirmDialog = false;
    this.confirmMenuId = -1;
  }

  goToEdit(id: number) {
    this.router.navigate(['/profile/edit-menu', id]);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
